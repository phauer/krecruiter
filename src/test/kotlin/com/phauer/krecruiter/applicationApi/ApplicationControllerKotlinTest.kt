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
import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.specs.FreeSpec
import io.mockk.clearAllMocks
import io.mockk.mockk
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import java.time.Clock

class ApplicationControllerKotlinTest : FreeSpec() {

    private val clock = mockk<Clock>()
    private val validationService = MockWebServer().apply { start() }
    private val controller = ApplicationController(
        dao = PostgreSQLInstance.jdbi.onDemand(),
        clock = clock,
        addressValidationClient = AddressValidationClient(
            client = TestObjects.httpClient,
            mapper = TestObjects.mapper,
            baseUrl = validationService.url("").toString()
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

                val actualResponseDTO = requestApplications()

                // TODO inspector or collection matchers
                Assertions.assertThat(actualResponseDTO).containsExactly(
                    ApplicationDTO(
                        id = 100,
                        fullName = "John Doe",
                        jobTitle = "Software Developer",
                        state = ApplicationState.RECEIVED,
                        dateCreated = 100.toInstant()
                    )
                )
            }
        }
        "asdf" {

        }
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