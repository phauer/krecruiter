package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.common.ApplicantEntity
import com.phauer.krecruiter.common.ApplicationEntity
import com.phauer.krecruiter.common.ApplicationState
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.customizer.Define
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
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
        ORDER by a.dateCreated
    """
    )
    @UseStringTemplateEngine
    fun findAllApplications(@Define state: ApplicationState?): List<ApplicationWithApplicantsEntity>

    @SqlUpdate(
        """INSERT INTO applicant(firstName, lastName, street, city, dateCreated)
        VALUES (:firstName, :lastName, :street, :city, :dateCreated)"""
    )
    @GetGeneratedKeys
    fun createApplicant(firstName: String, lastName: String, street: String, city: String, dateCreated: Instant): ApplicantEntity

    @SqlUpdate(
        """INSERT INTO application(applicantId, jobTitle, state, dateCreated)
        VALUES (:applicantId, :jobTitle, :state, :dateCreated)"""
    )
    @GetGeneratedKeys
    fun createApplication(jobTitle: String, applicantId: Int, state: ApplicationState, dateCreated: Instant): ApplicationEntity
}

data class ApplicationWithApplicantsEntity(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val jobTitle: String,
    val state: ApplicationState,
    val dateCreated: Instant
)