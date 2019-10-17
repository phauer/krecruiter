package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.common.ApiPaths
import com.phauer.krecruiter.common.ApplicationState
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Clock

@RestController
@RequestMapping(ApiPaths.applications)
class ApplicationController(
    private val dao: ApplicationDAO,
    private val clock: Clock
) {
    @GetMapping
    fun getApplications(
        @RequestParam state: ApplicationState?
    ): List<ApplicationDTO> {
        val applicationEntities = dao.findAllApplications(state)
        return applicationEntities.map { it.mapToDto() }
    }

    // TODO AddressValidationService, validation

    @PostMapping
    fun createApplication(
        @RequestBody application: ApplicationCreationDTO
    ) {
        val now = clock.instant()
        val applicant = dao.createApplicant(application.firstName, application.lastName, application.street, application.city, now)
        dao.createApplication(application.jobTitle, applicant.id, ApplicationState.RECEIVED, now)
    }

}

private fun ApplicationWithApplicantsEntity.mapToDto() = ApplicationDTO(
    id = this.id,
    fullName = "${this.firstName} ${this.lastName}",
    jobTitle = this.jobTitle,
    state = this.state,
    dateCreated = this.dateCreated
)

