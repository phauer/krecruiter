package com.phauer.recruitingapp.common

// TODO: timestamps?

data class ApplicationEntity(
    val id: Int,
    val applicant: ApplicantEntity,
    val jobTitle: String,
    val status: ApplicationState,
    val isInternalApplication: Boolean
)

// TODO add reasonable nullable types

data class ApplicantEntity(
    val firstName: String,
    val lastName: String,
    val address: AddressEntity
)

data class AddressEntity(
    val street: String,
    val city: String,
    val zipCode: String
)

enum class ApplicationState {
    APPLICATION_RECEIVED,
    REJECTED,
    EMPLOYED
}
