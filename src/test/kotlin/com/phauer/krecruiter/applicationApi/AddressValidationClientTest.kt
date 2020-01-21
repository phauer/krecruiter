package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.common.Outcome
import com.phauer.krecruiter.util.TestObjects
import com.phauer.krecruiter.util.createStartedMockServer
import com.phauer.krecruiter.util.enqueueValidationResponse
import com.phauer.krecruiter.util.getUrl
import com.phauer.krecruiter.util.reset
import okhttp3.mockwebserver.MockResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AddressValidationClientTest {

    private val validationService = createStartedMockServer()
    private val client = AddressValidationClient(
        client = TestObjects.httpClient,
        mapper = TestObjects.mapper,
        baseUrl = validationService.getUrl()
    )

    @BeforeEach
    fun clear() {
        validationService.reset()
    }

    @Test
    fun `pass a 200 response to the caller with a success object`() {
        validationService.enqueueValidationResponse(code = 200, valid = true, address = "Long Street")

        val response = client.validateAddress("Long Street", "Leipzig")

        assertThat(response).isInstanceOfSatisfying(Outcome.Success::class.java) { success ->
            val expectedDTO = AddressValidationResponseDTO(address = "Long Street", valid = true)
            assertThat(success.value).isEqualTo(expectedDTO)
        }
    }


    @Test
    fun `return an error object if the validation service returns a 500`() {
        validationService.enqueue(MockResponse().setResponseCode(500))

        val response = client.validateAddress("Long Street", "Leipzig")

        assertThat(response).isInstanceOf(Outcome.Error::class.java)
    }
}


