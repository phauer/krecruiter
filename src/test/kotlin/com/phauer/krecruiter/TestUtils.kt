package com.phauer.krecruiter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType
import com.phauer.krecruiter.applicationApi.ApplicationCreationDTO
import com.phauer.krecruiter.applicationApi.ApplicationDTO
import com.phauer.krecruiter.common.ApplicantEntity
import com.phauer.krecruiter.common.ApplicationEntity
import com.phauer.krecruiter.common.ApplicationState
import com.phauer.krecruiter.common.ExceptionControllerAdvice
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.QueueDispatcher
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.servlet.view.InternalResourceViewResolver
import java.time.Instant
import java.util.UUID

object TestObjects {
    val mapper: ObjectMapper = SpringConfiguration().objectMapper()
    val httpClient = SpringConfiguration().httpClient()
    val applicationDtoListType: CollectionType = mapper.typeFactory.constructCollectionType(List::class.java, ApplicationDTO::class.java)
}

fun <T> T.toJson(): String = TestObjects.mapper.writeValueAsString(this)

inline fun <reified T> String.toObject(): T = TestObjects.mapper.readValue(this, T::class.java)

fun Int.toInstant(): Instant = Instant.ofEpochSecond(this.toLong())

fun Int.toUUID(): UUID = UUID.fromString("00000000-0000-0000-a000-${this.toString().padStart(11, '0')}")

fun createMockMvc(controller: Any) = MockMvcBuilders
    .standaloneSetup(controller)
    .setViewResolvers(InternalResourceViewResolver())
    .setControllerAdvice(ExceptionControllerAdvice())
    .build()

fun MockWebServer.reset() {
    dispatcher = QueueDispatcher()
}

fun createApplicationEntity(
    id: Int,
    applicantId: Int,
    jobTitle: String = "Test Job Title",
    state: ApplicationState = ApplicationState.RECEIVED,
    dateCreated: Instant = 1.toInstant()
) = ApplicationEntity(
    id = id,
    applicantId = applicantId,
    jobTitle = jobTitle,
    state = state,
    dateCreated = dateCreated
)

fun createApplicantEntity(
    id: Int,
    firstName: String = "Peter",
    lastName: String = "Meier",
    street: String = "Grimmaische Straße",
    city: String = "Leipzig",
    dateCreated: Instant = 1.toInstant()
) = ApplicantEntity(
    id = id,
    firstName = firstName,
    lastName = lastName,
    street = street,
    city = city,
    dateCreated = dateCreated
)

fun createApplicantEntity(
    firstName: String = "John",
    lastName: String = "Doe",
    street: String = "Short Ave",
    city: String = "Cologne",
    jobTitle: String = "Product Owner"
) = ApplicationCreationDTO(
    firstName = firstName,
    lastName = lastName,
    street = street,
    city = city,
    jobTitle = jobTitle
)