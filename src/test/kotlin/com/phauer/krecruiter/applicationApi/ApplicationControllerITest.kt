package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.PostgreSQLInstance
import com.phauer.krecruiter.TestDAO
import com.phauer.krecruiter.TestObjects
import com.phauer.krecruiter.common.ApplicationState
import com.phauer.krecruiter.createApplicantEntity
import com.phauer.krecruiter.createApplicationEntity
import com.phauer.krecruiter.createMockMvc
import com.phauer.krecruiter.toInstant
import io.mockk.clearMocks
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Clock

internal class ApplicationControllerITest {

    // TODO start testing (mapping, order, filter) and develop the service this way.
    // TODO test: applicants with multiple applications with status X
    // TODO add a Scheduler - maybe it will email
    // TODO POST + calling the addressValidation service
    // TODO clock
    // TODO more complexity required? rename applicant to person and introduce recruiter and hiringManager as parts of the application

    private val clock = mockk<Clock>()
    private val testDAO = TestDAO(PostgreSQLInstance.jdbi)
    private lateinit var mvc: MockMvc

    @BeforeAll
    fun schemaSetup() {
        testDAO.recreateSchema()
    }

    @BeforeEach
    fun setup() {
        clearMocks(clock)
        testDAO.clearTables()
        val controller = ApplicationController(
            dao = PostgreSQLInstance.jdbi.onDemand(),
            clock = clock
        )
        mvc = createMockMvc(controller)
    }

    @Test
    fun `return all fields of applications and applicant`() {
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
            ),
            createApplicationEntity(
                id = 200,
                applicantId = 1,
                jobTitle = "QA Engineer",
                state = ApplicationState.REJECTED,
                dateCreated = 200.toInstant()
            )
        )

        val actualResponseDTO = requestApplications()

        assertThat(actualResponseDTO).containsExactlyInAnyOrder(
            ApplicationDTO(
                id = 100,
                fullName = "John Doe",
                jobTitle = "Software Developer",
                status = ApplicationState.RECEIVED,
                dateCreated = 100.toInstant()
            ),
            ApplicationDTO(
                id = 200,
                fullName = "John Doe",
                jobTitle = "QA Engineer",
                status = ApplicationState.REJECTED,
                dateCreated = 200.toInstant()
            )
        )
    }

    private fun requestApplications(): List<ApplicationDTO> {
        val responseString = mvc.get("/applications")
            .andExpect {
                status { isOk }
                content { contentType(MediaType.APPLICATION_JSON) }
            }.andReturn().response.contentAsString
        return TestObjects.mapper.readValue(responseString, TestObjects.applicationDtoListType)
    }
}