package com.phauer.krecruiter.util

import com.phauer.krecruiter.applicationApi.ApplicationCreationDTO
import com.phauer.krecruiter.common.ApplicantEntity
import com.phauer.krecruiter.common.ApplicationEntity
import com.phauer.krecruiter.common.ApplicationState
import java.time.Instant

fun createApplicationEntity(
    id: Int,
    applicantId: Int,
    jobTitle: String = "Test Job Title",
    state: ApplicationState = ApplicationState.RECEIVED,
    dateCreated: Instant = 1.toInstant(),
    attachments: String? = null
) = ApplicationEntity(
    id = id,
    applicantId = applicantId,
    jobTitle = jobTitle,
    state = state,
    dateCreated = dateCreated,
    attachments = attachments
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

fun createApplicationDTO(
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