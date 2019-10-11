package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.common.ApplicationState
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Clock

@RestController
@RequestMapping("/applications")
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

}

private fun ApplicationWithApplicantsEntity.mapToDto() = ApplicationDTO(
    id = this.id,
    fullName = "${this.firstName} ${this.lastName}",
    jobTitle = this.jobTitle,
    state = this.state,
    dateCreated = this.dateCreated
)

