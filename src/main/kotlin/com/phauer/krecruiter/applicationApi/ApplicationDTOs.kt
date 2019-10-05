package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.common.ApplicationState
import java.time.Instant

data class ApplicationDTO(
    val id: Int,
    val fullName: String,
    val jobTitle: String,
    val status: ApplicationState,
    val dateCreated: Instant
)