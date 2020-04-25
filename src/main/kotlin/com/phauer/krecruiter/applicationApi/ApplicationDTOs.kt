package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.common.ApplicationState
import java.time.Instant

data class ApplicationDTO(
    val id: Int,
    val fullName: String,
    val jobTitle: String,
    val state: ApplicationState,
    val dateCreated: Instant,
    val attachments: Map<String, String>
)

data class ApplicationCreationDTO(
    val firstName: String,
    val lastName: String,
    val street: String,
    val city: String,
    val jobTitle: String
)
