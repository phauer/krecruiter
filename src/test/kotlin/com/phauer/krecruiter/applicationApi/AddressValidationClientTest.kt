package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.TestObjects
import com.phauer.krecruiter.common.Outcome
import com.phauer.krecruiter.reset
import com.phauer.krecruiter.toJson
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

internal class AddressValidationClientTest {

    private val validationService = MockWebServer().apply { start() }
    private val client = AddressValidationClient(
        client = TestObjects.httpClient,
        mapper = TestObjects.mapper,
        baseUrl = validationService.url("").toString()
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

private fun MockWebServer.enqueueValidationResponse(code: Int, valid: Boolean, address: String) {
    val response = MockResponse()
        .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .setBody(AddressValidationResponseDTO(valid = valid, address = address).toJson())
        .setResponseCode(code)
    enqueue(response)
}
