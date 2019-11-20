package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.PostgreSQLInstance
import com.phauer.krecruiter.TestDAO
import com.phauer.krecruiter.TestObjects
import com.phauer.krecruiter.common.ApiPaths
import com.phauer.krecruiter.common.ApplicationState
import com.phauer.krecruiter.createApplicantEntity
import com.phauer.krecruiter.createApplicationEntity
import com.phauer.krecruiter.createMockMvc
import com.phauer.krecruiter.reset
import com.phauer.krecruiter.toInstant
import com.phauer.krecruiter.toJson
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Clock
import java.time.Instant

internal class ApplicationControllerITest {

    // TODO posting invalid json or missing requried fields -> ugly jackson response message
    // TODO check out java testing guide: what else can we test that require special assertions.
    // TODO POST resource: location header
    // TODO add a Scheduler - maybe it will email
    // TODO more complexity required? rename applicant to person and introduce recruiter and hiringManager as parts of the application

    private val clock = mockk<Clock>()
    private val validationService = MockWebServer().apply { start() }
    private val controller = ApplicationController(
        dao = PostgreSQLInstance.jdbi.onDemand(),
        clock = clock,
        addressValidationClient = AddressValidationClient(TestObjects.httpClient, TestObjects.mapper, validationService.url("").toString())
    )
    private val mvc = createMockMvc(controller)
    private val testDAO = TestDAO(PostgreSQLInstance.jdbi)

    @BeforeAll
    fun schemaSetup() {
        testDAO.recreateSchema()
    }

    @BeforeEach
    fun clear() {
        clearMocks(clock)
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

            val actualResponseDTO = requestApplications()

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

            val actualResponseDTO = requestApplications(state = ApplicationState.REJECTED)

            assertThat(actualResponseDTO)
                .extracting<Int>(ApplicationDTO::id)
                .containsOnly(100, 200)
        }

        @Test
        fun `return all when application state is not set`() {
            insertApplicationWithApplicant(id = 100, state = ApplicationState.REJECTED)
            insertApplicationWithApplicant(id = 200, state = ApplicationState.INVITED_TO_INTERVIEW)

            val actualResponseDTO = requestApplications(state = null)

            assertThat(actualResponseDTO)
                .extracting<Int>(ApplicationDTO::id)
                .containsOnly(100, 200)
        }


        @Test
        fun `order by dateCreated`() {
            insertApplicationWithApplicant(id = 100, dateCreated = 100.toInstant())
            insertApplicationWithApplicant(id = 200, dateCreated = 200.toInstant())
            insertApplicationWithApplicant(id = 300, dateCreated = 3.toInstant())

            val actualResponseDTO = requestApplications()

            assertThat(actualResponseDTO)
                .extracting<Int>(ApplicationDTO::id)
                .containsExactly(300, 100, 200)
        }

        private fun requestApplications(
            state: ApplicationState? = null
        ): List<ApplicationDTO> {
            val responseString = mvc.get(ApiPaths.applications) {
                if (state != null) {
                    param("state", state.toString())
                }
            }.andExpect {
                status { isOk }
                content { contentType(MediaType.APPLICATION_JSON) }
            }.andReturn().response.contentAsString
            return TestObjects.mapper.readValue(responseString, TestObjects.applicationDtoListType)
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

            with(testDAO.findOneApplication()) {
                assertThat(jobTitle).isEqualTo("Software Engineer")
                assertThat(state).isEqualTo(ApplicationState.RECEIVED)
                assertThat(dateCreated).isEqualTo(1.toInstant())
            }
            with(testDAO.findOneApplicant()) {
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
        fun `return server error if the address validation service is not available`() {
            validationService.enqueue(MockResponse().setResponseCode(500))
            val requestApplication = createApplicantEntity()

            val response = postApplicationAndGetResponse(requestApplication)

            assertThat(response.status).isEqualTo(500)
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

private fun MockWebServer.enqueueValidationResponse(code: Int, valid: Boolean) {
    val response = MockResponse()
        .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .setBody(AddressValidationResponseDTO(valid = valid, address = "test address").toJson())
        .setResponseCode(code)
    enqueue(response)
}

