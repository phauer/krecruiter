package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.common.ApiPaths
import com.phauer.krecruiter.common.ApplicantEntity
import com.phauer.krecruiter.common.ApplicationEntity
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
import com.phauer.krecruiter.util.reset
import com.phauer.krecruiter.util.toInstant
import com.phauer.krecruiter.util.toJson
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.PropTestListener
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import java.time.Clock
import java.time.Instant

class ApplicationControllerKoTest : FreeSpec() {

    private val clock = mockk<Clock>()
    private val validationService = createStartedMockServer()
    private val controller = ApplicationController(
        dao = ApplicationDAO(PostgreSQLInstance.jdbi),
        clock = clock,
        addressValidationClient = AddressValidationClient(
            client = TestObjects.httpClient,
            mapper = TestObjects.mapper,
            baseUrl = validationService.getUrl()
        ),
        mapper = TestObjects.mapper
    )
    private val mvc = createMockMvc(controller)
    private val testDAO = PostgreSQLInstance.jdbi.onDemand<TestDAO>()

    // define setup code to make it reuseable for normal and prop-based tests.
    private val beforeTestSetup = {
        clearAllMocks()
        testDAO.clearTables()
        validationService.reset()
    }
    // for running setup code before a property test
    private val propTestConfig = PropTestConfig(listeners = listOf(object : PropTestListener {
        override suspend fun beforeTest() {
            beforeTestSetup()
        }
    }))

    init {
        // for running setup code before normal tests (not property tests)
        beforeTest{
            beforeTestSetup()
        }
        "Get Applications" - {
            "return all relevant fields from database" {
                // ...
            }

            // other tests coming here...
        }
        "Create Application" - {
            "posting an application creates an application and an applicant entry in the database with the posted values and the current timestamp" {
                // ...
            }

            "Create an application with randomized data" {
                checkAll(
                    iterations = 20,
                    config = propTestConfig,
                    Arb.string(maxSize = 60),
                    Arb.string(maxSize = 60),
                    Arb.string(maxSize = 60),
                    Arb.string(maxSize = 30),
                    Arb.string(maxSize = 120)
                ) { firstName, lastName, street, city, jobTitle ->
                    val requestedApplication = ApplicationCreationDTO(
                        firstName = firstName,
                        lastName = lastName,
                        street = street,
                        city = city,
                        jobTitle = jobTitle
                    )
                    mockClock(1.toInstant())
                    validationService.enqueueValidationResponse(code = 200, valid = true)

                    postApplicationAndExpect201(requestedApplication)

                    findOneApplication().asClue {
                        it.jobTitle shouldBe requestedApplication.jobTitle
                        it.state shouldBe ApplicationState.RECEIVED
                        it.dateCreated shouldBe 1.toInstant()
                    }
                    findOneApplicant().asClue {
                        it.firstName shouldBe requestedApplication.firstName
                        it.lastName shouldBe requestedApplication.lastName
                        it.city shouldBe requestedApplication.city
                        it.street shouldBe requestedApplication.street
                        it.dateCreated shouldBe 1.toInstant()
                    }
                }
            }

            "dont create an application and return a 400 if an required JSON field is missing." - {
                forAll( // there are two forall methods. import the one in the suspend package
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
                forAll( // there are two forall methods. import the one in the suspend package
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

            // other test coming here...
        }
    }

    private fun findOneApplication(): ApplicationEntity {
        val application = testDAO.findOneApplication()
        application.shouldNotBeNull()
        return application
    }

    private fun findOneApplicant(): ApplicantEntity {
        val applicant = testDAO.findOneApplicant()
        applicant.shouldNotBeNull()
        return applicant
    }

    private fun postApplicationAndExpect400(json: String) {
        mvc.post(ApiPaths.applications) {
            content = json
            contentType = MediaType.APPLICATION_JSON
        }
            .andExpect { status { is4xxClientError() } }
    }

    private fun postApplicationAndExpect201(requestApplication: ApplicationCreationDTO) = postApplication(requestApplication)
        .andExpect { status { isCreated() } }.andReturn().response

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