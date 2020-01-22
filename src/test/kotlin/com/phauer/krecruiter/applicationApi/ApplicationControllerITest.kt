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
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import java.time.Clock
import java.time.Instant
import java.util.stream.Stream

internal class ApplicationControllerITest {

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
    private val testDAO = PostgreSQLInstance.jdbi.onDemand<TestDAO>()

    @BeforeEach
    fun clear() {
        clearAllMocks()
        testDAO.clearTables()
        validationService.reset()
    }

    @Nested
    inner class GetApplications {

        @Test
        fun `return all relevant fields from database`() {
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

            assertThat(actualResponseDTO).containsExactly(
                ApplicationDTO(
                    id = 100,
                    fullName = "John Doe",
                    jobTitle = "Software Developer",
                    state = ApplicationState.RECEIVED,
                    dateCreated = 100.toInstant()
                )
            )
        }

        @Test
        fun `filter by application state`() {
            insertApplicationWithApplicant(id = 100, state = ApplicationState.REJECTED)
            insertApplicationWithApplicant(id = 200, state = ApplicationState.REJECTED)
            insertApplicationWithApplicant(id = 300, state = ApplicationState.INVITED_TO_INTERVIEW)
            insertApplicationWithApplicant(id = 400, state = ApplicationState.EMPLOYED)
            insertApplicationWithApplicant(id = 500, state = ApplicationState.RECEIVED)

            val actualResponseDTO = mvc.requestApplications(state = ApplicationState.REJECTED)

            assertThat(actualResponseDTO)
                .extracting<Int>(ApplicationDTO::id)
                .containsOnly(100, 200)
        }

        @Test
        fun `return all when application state is not set`() {
            insertApplicationWithApplicant(id = 100, state = ApplicationState.REJECTED)
            insertApplicationWithApplicant(id = 200, state = ApplicationState.INVITED_TO_INTERVIEW)

            val actualResponseDTO = mvc.requestApplications(state = null)

            assertThat(actualResponseDTO)
                .extracting<Int>(ApplicationDTO::id)
                .containsOnly(100, 200)
        }


        @Test
        fun `order by dateCreated`() {
            insertApplicationWithApplicant(id = 100, dateCreated = 100.toInstant())
            insertApplicationWithApplicant(id = 200, dateCreated = 200.toInstant())
            insertApplicationWithApplicant(id = 300, dateCreated = 3.toInstant())

            val actualResponseDTO = mvc.requestApplications()

            assertThat(actualResponseDTO)
                .extracting<Int>(ApplicationDTO::id)
                .containsExactly(300, 100, 200)
        }
    }

    @Nested
    inner class CreateApplication {

        @Test
        fun `posting an application creates an application and an applicant entry in the database with the posted values and the current timestamp`() {
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

            with(testDAO.findOneApplication()!!) {
                assertThat(jobTitle).isEqualTo("Software Engineer")
                assertThat(state).isEqualTo(ApplicationState.RECEIVED)
                assertThat(dateCreated).isEqualTo(1.toInstant())
            }
            with(testDAO.findOneApplicant()!!) {
                assertThat(firstName).isEqualTo("Anna")
                assertThat(lastName).isEqualTo("Schmidt")
                assertThat(city).isEqualTo("Leipzig")
                assertThat(street).isEqualTo("Long Street")
                assertThat(dateCreated).isEqualTo(1.toInstant())
            }
        }

        @Test
        fun `reject application with invalid address`() {
            validationService.enqueueValidationResponse(code = 200, valid = false)
            val requestApplication = createApplicantEntity()

            val response = postApplicationAndGetResponse(requestApplication)

            assertThat(response.status).isEqualTo(400)
            assertThat(response.contentAsString).containsIgnoringCase("invalid address")
        }

        @Test
        fun `return server error if the address validation service returns 500`() {
            validationService.enqueue(MockResponse().setResponseCode(500))
            val requestApplication = createApplicantEntity()

            val response = postApplicationAndGetResponse(requestApplication)

            assertThat(response.status).isEqualTo(500)
        }

        @ParameterizedTest
        @MethodSource("missingFieldDtoProvider")
        fun `dont create an application and return a 400 if an required JSON field is missing`(dtoWithMissingField: MissingFieldApplicationDTO) {
            postApplicationAndExpect400(dtoWithMissingField.toJson())
            assertThat(testDAO.findOneApplication()).isNull()
            assertThat(testDAO.findOneApplicant()).isNull()
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                """""",
                """asdfasfd""",
                """2""",
                """{}""",
                """{"1":"a"}""",
                """[]"""
            ]
        )
        fun `dont create an application and return a 400 if an invalid JSON is passed`(invalidJson: String) {
            postApplicationAndExpect400(invalidJson)
            assertThat(testDAO.findOneApplication()).isNull()
            assertThat(testDAO.findOneApplicant()).isNull()
        }

        private fun missingFieldDtoProvider() = Stream.of(
            MissingFieldApplicationDTO(firstName = "name1", lastName = "name2", street = "street", city = "city", jobTitle = null)
            , MissingFieldApplicationDTO(firstName = "name1", lastName = "name2", street = "street", city = null, jobTitle = "title")
            , MissingFieldApplicationDTO(firstName = "name1", lastName = "name2", street = null, city = "city", jobTitle = "title")
            , MissingFieldApplicationDTO(firstName = "name1", lastName = null, street = "street", city = "city", jobTitle = "title")
            , MissingFieldApplicationDTO(firstName = null, lastName = "name2", street = "street", city = "city", jobTitle = "title")
        )

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

data class MissingFieldApplicationDTO(
    val firstName: String?,
    val lastName: String?,
    val street: String?,
    val city: String?,
    val jobTitle: String?
)