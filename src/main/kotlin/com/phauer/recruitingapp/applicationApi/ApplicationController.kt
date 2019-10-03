package com.phauer.recruitingapp.applicationApi

import com.phauer.recruitingapp.common.ApplicationEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApplicationController(
    private val dao: ApplicationDAO
) {

    // TODO introduce entity/table applicants with address
    // TODO POST + calling the addressValidation service

    // test: mapping, order, filter
    @GetMapping("/applications")
    fun getApplications(): List<ApplicationDTO> {
        val applicationEntities = dao.findAllApplications()
        return applicationEntities.map { it.mapToDto() }
    }

}

private fun ApplicationEntity.mapToDto() = ApplicationDTO(
    id = this.id,
    fullName = "${this.firstName} ${this.lastName}",
    jobTitle = this.jobTitle,
    status = this.status,
    dateCreated = this.dateCreated
)

