package com.phauer.krecruiter.applicationApi

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Clock

@RestController
class ApplicationController(
    private val dao: ApplicationDAO,
    private val clock: Clock
) {
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

