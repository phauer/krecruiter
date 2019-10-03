package com.phauer.recruitingapp.common

import java.time.Instant

data class ApplicationEntity(
    val id: Int,
    val name: String,
    val jobTitle: String,
    val status: ApplicationState,
    val dateCreated: Instant
)

enum class ApplicationState {
    RECEIVED,
    INVITED_TO_INTERVIEW,
    REJECTED,
    EMPLOYED
}
