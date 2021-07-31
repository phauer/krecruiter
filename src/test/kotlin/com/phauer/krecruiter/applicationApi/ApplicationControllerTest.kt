package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.common.ApiPaths
import com.phauer.krecruiter.common.ApplicationState
import com.phauer.krecruiter.common.Outcome
import com.phauer.krecruiter.util.TestObjects
import com.phauer.krecruiter.util.createApplicantEntity
import com.phauer.krecruiter.util.createMockMvc
import com.phauer.krecruiter.util.requestApplications
import com.phauer.krecruiter.util.toInstant
import com.phauer.krecruiter.util.toJson
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContainIgnoringCase
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import java.time.Clock
import java.time.Instant

internal class ApplicationControllerTest {

    private val clock = mockk<Clock>()
    private val addressValidationClient = mockk<AddressValidationClient>()
    private val dao = mockk<ApplicationDAO>()
    private val controller = ApplicationController(
        dao = dao,
        clock = clock,
        addressValidationClient = addressValidationClient,
        mapper = TestObjects.mapper
    )
    private val mvc = createMockMvc(controller)

    @BeforeEach
    fun clear() {
        clearAllMocks()
    }

    @Nested
    inner class GetApplications {

        @Test
        fun `return all relevant fields from database`() {
            every { dao.findAllApplications(any()) } returns listOf(
                ApplicationWithApplicantsEntity(
                    id = 1,
                    firstName = "John",
                    lastName = "Doe",
                    jobTitle = "Software Developer",
                    state = ApplicationState.RECEIVED,
                    dateCreated = 100.toInstant(),
                    attachments = null
                )
            )

            val actualResponseDTO = mvc.requestApplications()

            actualResponseDTO.shouldContainExactly(
                ApplicationDTO(
                    id = 1,
                    fullName = "John Doe",
                    jobTitle = "Software Developer",
                    state = ApplicationState.RECEIVED,
                    dateCreated = 100.toInstant(),
                    attachments = mapOf()
                )
            )
        }
    }

    @Nested
    inner class CreateApplication {

        @Test
        fun `posting an application creates an application and an applicant entry in the database with the posted values and the current timestamp`() {
            mockClock(1.toInstant())
            every { addressValidationClient.validateAddress(any(), any()) } returns Outcome.Success(
                value = AddressValidationResponseDTO(
                    address = "street",
                    valid = true
                )
            )
            every { dao.createApplicant(any(), any(), any(), any(), any()) } returns 1
            every { dao.createApplication(any(), any(), any(), any()) } returns 10

            val requestApplication = createApplicantEntity(
                firstName = "Anna",
                lastName = "Schmidt",
                street = "Long Street",
                city = "Leipzig",
                jobTitle = "Software Engineer"
            )
            postApplicationAndExpect201(requestApplication)

            verify {
                dao.createApplicant("Anna", "Schmidt", "Long Street", "Leipzig", 1.toInstant())
                dao.createApplication("Software Engineer", 1, ApplicationState.RECEIVED, 1.toInstant())
            }
        }

        @Test
        fun `reject application with invalid address`() {
            every { addressValidationClient.validateAddress(any(), any()) } returns Outcome.Success(
                value = AddressValidationResponseDTO(
                    address = "street",
                    valid = false
                )
            )
            val requestApplication = createApplicantEntity()

            val response = postApplicationAndGetResponse(requestApplication)

            response.status shouldBe 400
            response.contentAsString shouldContainIgnoringCase "invalid address"
        }

        @Test
        fun `return server error if the request to the address validation service is not successfully`() {
            every { addressValidationClient.validateAddress(any(), any()) } returns Outcome.Error(
                message = "Request Failed",
                cause = null
            )
            val requestApplication = createApplicantEntity()

            val response = postApplicationAndGetResponse(requestApplication)

            response.status shouldBe 500
        }

        private fun postApplicationAndGetResponse(requestApplication: ApplicationCreationDTO) = postApplication(requestApplication)
            .andReturn().response

        private fun postApplicationAndExpect201(requestApplication: ApplicationCreationDTO) = postApplication(requestApplication)
            .andExpect { status { isCreated() } }.andReturn().response

        private fun postApplication(requestApplication: ApplicationCreationDTO) = mvc.post(ApiPaths.applications) {
            content = requestApplication.toJson()
            contentType = MediaType.APPLICATION_JSON
        }
    }

    private fun mockClock(time: Instant = 1.toInstant()) {
        every { clock.instant() } returns time
    }
}
