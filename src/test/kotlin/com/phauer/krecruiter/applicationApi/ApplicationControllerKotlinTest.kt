package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.common.ApiPaths
import com.phauer.krecruiter.common.ApplicationState
import com.phauer.krecruiter.util.PostgreSQLInstance
import com.phauer.krecruiter.util.TestDAO
import com.phauer.krecruiter.util.TestObjects
import com.phauer.krecruiter.util.createApplicantEntity
import com.phauer.krecruiter.util.createApplicationEntity
import com.phauer.krecruiter.util.createMockMvc
import com.phauer.krecruiter.util.createStartedMockServer
import com.phauer.krecruiter.util.enqueueValidationResponse
import com.phauer.krecruiter.util.getUrl
import com.phauer.krecruiter.util.requestApplications
import com.phauer.krecruiter.util.reset
import com.phauer.krecruiter.util.toInstant
import com.phauer.krecruiter.util.toJson
import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.data.suspend.forall
import io.kotlintest.matchers.asClue
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.collections.shouldContainInOrder
import io.kotlintest.matchers.string.shouldContainIgnoringCase
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.kotlintest.tables.row
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import java.time.Clock
import java.time.Instant

class ApplicationControllerKotlinTest : FreeSpec() {

    private val clock = mockk<Clock>()
    private val validationService = createStartedMockServer()
    private val controller = ApplicationController(
        dao = ApplicationDAO(PostgreSQLInstance.jdbi),
        clock = clock,
        addressValidationClient = AddressValidationClient(
            client = TestObjects.httpClient,
            mapper = TestObjects.mapper,
            baseUrl = validationService.getUrl()
        )
    )
    private val mvc = createMockMvc(controller)
    private val testDAO = TestDAO(PostgreSQLInstance.jdbi)


    override fun beforeSpec(spec: Spec) {
        testDAO.recreateSchema()
    }

    override fun beforeTest(testCase: TestCase) {
        clearAllMocks()
        testDAO.clearTables()
        validationService.reset()
    }

    init {
        "Get Applications" - {
            "return all relevant fields from database" {
                testDAO.insert(
                    createApplicantEntity(id = 1, firstName = "John", lastName = "Doe")
                )
                testDAO.insert(
                    createApplicationEntity(
                        id = 100,
                        applicantId = 1,
                        jobTitle = "Software Developer",
                        state = ApplicationState.RECEIVED,
                        dateCreated = 100.toInstant()
                    )
                )

                val actualResponseDTO = mvc.requestApplications()

                actualResponseDTO shouldContain ApplicationDTO(
                    id = 100,
                    fullName = "John Doe",
                    jobTitle = "Software Developer",
                    state = ApplicationState.RECEIVED,
                    dateCreated = 100.toInstant()
                )
            }

            "filter by application state" {
                insertApplicationWithApplicant(id = 100, state = ApplicationState.REJECTED)
                insertApplicationWithApplicant(id = 200, state = ApplicationState.REJECTED)
                insertApplicationWithApplicant(id = 300, state = ApplicationState.INVITED_TO_INTERVIEW)
                insertApplicationWithApplicant(id = 400, state = ApplicationState.EMPLOYED)
                insertApplicationWithApplicant(id = 500, state = ApplicationState.RECEIVED)

                mvc.requestApplications(state = ApplicationState.REJECTED).asClue {
                    it.map(ApplicationDTO::id).shouldContainAll(100, 200)
                }
            }

            "return all when application state is not set" {
                insertApplicationWithApplicant(id = 100, state = ApplicationState.REJECTED)
                insertApplicationWithApplicant(id = 200, state = ApplicationState.INVITED_TO_INTERVIEW)

                mvc.requestApplications(state = null).asClue {
                    it.map(ApplicationDTO::id).shouldContainAll(100, 200)
                }
            }

            "order by dateCreated" {
                insertApplicationWithApplicant(id = 100, dateCreated = 100.toInstant())
                insertApplicationWithApplicant(id = 200, dateCreated = 200.toInstant())
                insertApplicationWithApplicant(id = 300, dateCreated = 3.toInstant())

                mvc.requestApplications().asClue {
                    it.map(ApplicationDTO::id).shouldContainInOrder(300, 100, 200)
                }
            }
        }
        "Create Application" - {
            "posting an application creates an application and an applicant entry in the database with the posted values and the current timestamp" {
                mockClock(1.toInstant())
                validationService.enqueueValidationResponse(code = 200, valid = true)
                val requestApplication = createApplicantEntity(
                    firstName = "Anna",
                    lastName = "Schmidt",
                    street = "Long Street",
                    city = "Leipzig",
                    jobTitle = "Software Engineer"
                )

                postApplicationAndExpect201(requestApplication)

                testDAO.findOneApplication().asClue {
                    it.shouldNotBeNull()
                    it.jobTitle shouldBe "Software Engineer"
                    it.state shouldBe ApplicationState.RECEIVED
                    it.dateCreated shouldBe 1.toInstant()
                }
                testDAO.findOneApplicant().asClue {
                    it.shouldNotBeNull()
                    it.firstName shouldBe "Anna"
                    it.lastName shouldBe "Schmidt"
                    it.city shouldBe "Leipzig"
                    it.street shouldBe "Long Street"
                    it.dateCreated shouldBe 1.toInstant()
                }
            }

            // currently, we can't put the test definition inside the assertAll-lambda (like we can do for table-driven tests)
            // see https://github.com/kotlintest/kotlintest/issues/717
            // we have to wait for KotlinTest 4.0
            "Create an application with randomized data" {
                assertAll(50, ApplicationCreationDTOGenerator()) { requestedApplication: ApplicationCreationDTO ->
                    beforeTest(mockk<TestCase>()) // beforeTest() cleanup is not executed automatically. -> KotlinTest 4.0
                    mockClock(1.toInstant())
                    validationService.enqueueValidationResponse(code = 200, valid = true)

                    postApplicationAndExpect201(requestedApplication)

                    testDAO.findOneApplication().asClue {
                        it.shouldNotBeNull()
                        it.jobTitle shouldBe requestedApplication.jobTitle
                        it.state shouldBe ApplicationState.RECEIVED
                        it.dateCreated shouldBe 1.toInstant()
                    }
                    testDAO.findOneApplicant().asClue {
                        it.shouldNotBeNull()
                        it.firstName shouldBe requestedApplication.firstName
                        it.lastName shouldBe requestedApplication.lastName
                        it.city shouldBe requestedApplication.city
                        it.street shouldBe requestedApplication.street
                        it.dateCreated shouldBe 1.toInstant()
                    }
                }
            }

            "reject application with invalid address" {
                validationService.enqueueValidationResponse(code = 200, valid = false)
                val requestApplication = createApplicantEntity()

                val response = postApplicationAndGetResponse(requestApplication)

                response.status shouldBe 400
                response.contentAsString shouldContainIgnoringCase "invalid address"
            }

            "return server error if the address validation service returns 500" {
                validationService.enqueue(MockResponse().setResponseCode(500))
                val requestApplication = createApplicantEntity()

                val response = postApplicationAndGetResponse(requestApplication)

                response.status shouldBe 500
            }

            "dont create an application and return a 400 if an required JSON field is missing." - {
                forall( // there are two forall methods. import the one in the suspend package
                    row(MissingFieldApplicationDTO(firstName = "name1", lastName = "name2", street = "street", city = "city", jobTitle = null)),
                    row(MissingFieldApplicationDTO(firstName = "name1", lastName = "name2", street = "street", city = null, jobTitle = "title")),
                    row(MissingFieldApplicationDTO(firstName = "name1", lastName = "name2", street = null, city = "city", jobTitle = "title")),
                    row(MissingFieldApplicationDTO(firstName = "name1", lastName = null, street = "street", city = "city", jobTitle = "title")),
                    row(MissingFieldApplicationDTO(firstName = null, lastName = "name2", street = "street", city = "city", jobTitle = "title"))
                ) { dtoWithMissingField: MissingFieldApplicationDTO ->
                    "DTO with missing field: $dtoWithMissingField" {
                        postApplicationAndExpect400(dtoWithMissingField.toJson())
                        testDAO.findOneApplication().shouldBeNull()
                        testDAO.findOneApplicant().shouldBeNull()
                    }
                }
            }

            "dont create an application and return a 400 if an invalid JSON is passed" - {
                forall( // there are two forall methods. import the one in the suspend package
                    row(""""""),
                    row("""asdfasfd"""),
                    row("""2"""),
                    row("""{}"""),
                    row("""{"1":"a"}"""),
                    row("""[]""")
                ) { invalidJson: String ->
                    "invalid json: $invalidJson" {
                        postApplicationAndExpect400(invalidJson)
                        testDAO.findOneApplication().shouldBeNull()
                        testDAO.findOneApplicant().shouldBeNull()
                    }
                }
            }
        }
    }

    private fun postApplicationAndExpect400(json: String) {
        mvc.post(ApiPaths.applications) {
            content = json
            contentType = MediaType.APPLICATION_JSON
        }
            .andExpect { status { isBadRequest } }
    }

    private fun postApplicationAndExpect201(requestApplication: ApplicationCreationDTO) = postApplication(requestApplication)
        .andExpect { status { isCreated } }.andReturn().response

    private fun postApplicationAndGetResponse(requestApplication: ApplicationCreationDTO) = postApplication(requestApplication)
        .andReturn().response

    private fun postApplication(requestApplication: ApplicationCreationDTO) = mvc.post(ApiPaths.applications) {
        content = requestApplication.toJson()
        contentType = MediaType.APPLICATION_JSON
    }

    private fun mockClock(time: Instant = 1.toInstant()) {
        every { clock.instant() } returns time
    }


    private fun insertApplicationWithApplicant(
        id: Int = 100,
        state: ApplicationState = ApplicationState.REJECTED,
        dateCreated: Instant = 1.toInstant()
    ) {
        testDAO.insert(createApplicantEntity(id = id, firstName = "John", lastName = "Doe"))
        testDAO.insert(createApplicationEntity(id = id, applicantId = id, state = state, dateCreated = dateCreated))
    }
}

class ApplicationCreationDTOGenerator : Gen<ApplicationCreationDTO> {
    // no edge cases for ApplicationCreationDTO
    override fun constants() = emptyList<ApplicationCreationDTO>()

    override fun random() = generateSequence {
        ApplicationCreationDTO(
            firstName = Gen.string(maxSize = 60).random().first(),
            lastName = Gen.string(maxSize = 60).random().first(),
            street = Gen.string(maxSize = 60).random().first(),
            city = Gen.string(maxSize = 30).random().first(),
            jobTitle = Gen.string(maxSize = 120).random().first()
        )
    }

}