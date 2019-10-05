package com.phauer.recruitingapp.applicationApi

import com.phauer.recruitingapp.PostgreSQLInstance
import com.phauer.recruitingapp.TestDAO
import com.phauer.recruitingapp.createMockMvc
import io.mockk.clearMocks
import io.mockk.mockk
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import java.time.Clock

internal class ApplicationControllerITest {

    // TODO start testing (mapping, order, filter) and develop the service this way.
    // TODO test: applicants with multiple applications with status X
    // TODO add a Scheduler - maybe it will email
    // TODO POST + calling the addressValidation service
    // TODO clock

    private val clock = mockk<Clock>()
    private val testDAO = PostgreSQLInstance.jdbi.onDemand<TestDAO>()
    private lateinit var mvc: MockMvc

    @BeforeEach
    fun setup() {
        clearMocks(clock)
//        testDAO.deleteAllEvents();
        val controller = ApplicationController(
            dao = PostgreSQLInstance.jdbi.onDemand(),
            clock = clock
        )
        mvc = createMockMvc(controller)
    }

    @Test
    fun `todo`() {

    }
}