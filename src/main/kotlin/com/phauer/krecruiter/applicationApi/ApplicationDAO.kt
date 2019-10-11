package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.common.ApplicationState
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.customizer.Define
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine
import java.time.Instant

interface ApplicationDAO : SqlObject {

    @SqlQuery(
        """
        SELECT a.id, p.firstName, p.lastName, a.jobTitle, a.state, a.dateCreated 
        FROM application as a LEFT JOIN applicant as p
        ON a.applicantId = p.id 
        WHERE 1 = 1
        <if(state)> AND a.state = '<state>' <endif>
    """
    )
    @UseStringTemplateEngine
    fun findAllApplications(@Define state: ApplicationState?): List<ApplicationWithApplicantsEntity>

}

data class ApplicationWithApplicantsEntity(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val jobTitle: String,
    val state: ApplicationState,
    val dateCreated: Instant
)