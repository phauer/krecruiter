package com.phauer.krecruiter

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.SocketPolicy
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.servlet.view.InternalResourceViewResolver
import java.time.Instant
import java.util.UUID

object TestObjects {
    val mapper = SpringConfiguration().objectMapper()
}

fun <T> T.toJson(): String = TestObjects.mapper.writeValueAsString(this)

fun createMockMvc(controller: Any) = MockMvcBuilders
    .standaloneSetup(controller)
    .setViewResolvers(InternalResourceViewResolver())
    .build()

//fun Long.toInstant(): Instant = Instant.ofEpochSecond(this)
fun Int.toInstant() = Instant.ofEpochSecond(this.toLong())

fun Int.toUUID() = UUID.fromString("00000000-0000-0000-a000-${this.toString().padStart(11, '0')}")

fun createMockResponse(
    responseBodyJSON: String = """{"exampleField": "exampleContent"}""",
    responseCode: Int = 200,
    socketPolicy: SocketPolicy = SocketPolicy.KEEP_OPEN
): MockResponse = MockResponse()
    .addHeader("Content-Type", "application/json; charset=utf-8")
    .addHeader("Cache-Control", "no-cache")
    .setBody(responseBodyJSON)
    .setResponseCode(responseCode)
    .apply { this.socketPolicy = socketPolicy }

