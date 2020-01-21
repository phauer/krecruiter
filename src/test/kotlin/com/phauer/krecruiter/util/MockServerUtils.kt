package com.phauer.krecruiter.util

import com.phauer.krecruiter.applicationApi.AddressValidationResponseDTO
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.QueueDispatcher
import org.springframework.http.MediaType

fun createStartedMockServer() = MockWebServer().apply { start() }

fun MockWebServer.getUrl(): String = url("").toString()

fun MockWebServer.reset() {
    dispatcher = QueueDispatcher()
}

fun MockWebServer.enqueueValidationResponse(
    code: Int,
    valid: Boolean,
    address: String = "test address"
) {
    val response = MockResponse()
        .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .setBody(AddressValidationResponseDTO(valid = valid, address = address).toJson())
        .setResponseCode(code)
    enqueue(response)
}