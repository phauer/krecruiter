package com.phauer.recruitingapp.applicationApi

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApplicationController(
    private val dao: ApplicationDAO
) {

    // TODO start testing (mapping, order, filter) and develop the service this way.
    // TODO test: applicants with multiple applications with status X
    // TODO add a Scheduler - maybe it will email
    // TODO POST + calling the addressValidation service

    @GetMapping("/applications")
    fun getApplications(): List<ApplicationDTO> {
        val applicationEntities = dao.findAllApplications()
        return applicationEntities.map { it.mapToDto() }
    }

}

private fun ApplicationWithApplicantsEntity.mapToDto() = ApplicationDTO(
    id = this.id,
    fullName = "${this.firstName} ${this.lastName}",
    jobTitle = this.jobTitle,
    status = this.status,
    dateCreated = this.dateCreated
)

