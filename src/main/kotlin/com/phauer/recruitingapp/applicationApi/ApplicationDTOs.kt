package com.phauer.recruitingapp.applicationApi

import com.phauer.recruitingapp.common.ApplicationState
import java.time.Instant

data class ApplicationDTO(
    val id: Int,
    val fullName: String,
    val jobTitle: String,
    val status: ApplicationState,
    val dateCreated: Instant
)