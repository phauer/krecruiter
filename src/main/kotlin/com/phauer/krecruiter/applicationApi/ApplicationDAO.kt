package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.common.ApplicationState
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.statement.SqlQuery
import java.time.Instant

interface ApplicationDAO : SqlObject {

    @SqlQuery(
        """
        SELECT a.id, p.firstName, p.lastName, a.jobTitle, a.status, a.dateCreated 
        FROM application as a LEFT JOIN applicant as p
        ON a.applicantId = p.id 
    """
    )
    fun findAllApplications(): List<ApplicationWithApplicantsEntity>

}

data class ApplicationWithApplicantsEntity(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val jobTitle: String,
    val status: ApplicationState,
    val dateCreated: Instant
)