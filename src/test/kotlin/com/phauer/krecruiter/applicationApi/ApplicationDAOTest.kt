package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.PostgreSQLInstance
import com.phauer.krecruiter.TestDAO
import com.phauer.krecruiter.common.ApplicationState
import com.phauer.krecruiter.createApplicantEntity
import com.phauer.krecruiter.createApplicationEntity
import com.phauer.krecruiter.toInstant
import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ApplicationDAOTest {

    private val dao: ApplicationDAO = PostgreSQLInstance.jdbi.onDemand()
    private val testDAO = TestDAO(PostgreSQLInstance.jdbi)

    @BeforeAll
    fun schemaSetup() {
        testDAO.recreateSchema()
    }

    @BeforeEach
    fun clear() {
        testDAO.clearTables()
    }

    @Test
    fun `filtering by ApplicationState should only return the applications with the requested state`() {
        insertApplicationWithApplicant(id = 100, state = ApplicationState.REJECTED)
        insertApplicationWithApplicant(id = 200, state = ApplicationState.REJECTED)
        insertApplicationWithApplicant(id = 300, state = ApplicationState.INVITED_TO_INTERVIEW)
        insertApplicationWithApplicant(id = 400, state = ApplicationState.EMPLOYED)
        insertApplicationWithApplicant(id = 500, state = ApplicationState.RECEIVED)

        val actualApplications = dao.findAllApplications(state = ApplicationState.REJECTED)

        assertThat(actualApplications)
            .extracting<Int>(ApplicationWithApplicantsEntity::id)
            .containsOnly(100, 200)
    }

    @Test
    fun `return all when application state is not set`() {
        insertApplicationWithApplicant(id = 100, state = ApplicationState.REJECTED)
        insertApplicationWithApplicant(id = 200, state = ApplicationState.INVITED_TO_INTERVIEW)

        val actualApplications = dao.findAllApplications(state = null)

        assertThat(actualApplications)
            .extracting<Int>(ApplicationWithApplicantsEntity::id)
            .containsOnly(100, 200)
    }


    @Test
    fun `order by dateCreated`() {
        insertApplicationWithApplicant(id = 100, dateCreated = 100.toInstant())
        insertApplicationWithApplicant(id = 200, dateCreated = 200.toInstant())
        insertApplicationWithApplicant(id = 300, dateCreated = 3.toInstant())

        val actualApplications = dao.findAllApplications(state = null)

        assertThat(actualApplications)
            .extracting<Int>(ApplicationWithApplicantsEntity::id)
            .containsExactly(300, 100, 200)
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