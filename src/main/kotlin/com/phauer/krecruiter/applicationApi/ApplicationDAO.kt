package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.common.ApplicationState
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.kotlin.withHandleUnchecked
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ApplicationDAO(
    private val jdbi: Jdbi
) {
    fun findAllApplications(state: ApplicationState?): List<ApplicationWithApplicantsEntity> {
        return jdbi.withHandleUnchecked<List<ApplicationWithApplicantsEntity>> {
            val sql = buildString {
                append(
                    """
                    SELECT a.id, p.firstName, p.lastName, a.jobTitle, a.state, a.dateCreated 
                    FROM application as a LEFT JOIN applicant as p
                    ON a.applicantId = p.id
                """
                )
                if (state != null) {
                    append("WHERE a.state = :state ")
                }
                append("ORDER by a.dateCreated")
            }
            it.createQuery(sql)
                .apply {
                    if (state != null) {
                        bind("state", state)
                    }
                }
                .mapTo<ApplicationWithApplicantsEntity>()
                .list()
        }
    }

    fun createApplicant(firstName: String, lastName: String, street: String, city: String, dateCreated: Instant): Int {
        return jdbi.withHandleUnchecked {
            it.createUpdate(
                """INSERT INTO applicant(firstName, lastName, street, city, dateCreated)
        VALUES (:firstName, :lastName, :street, :city, :dateCreated)"""
            )
                .bind("firstName", firstName)
                .bind("lastName", lastName)
                .bind("street", street)
                .bind("city", city)
                .bind("dateCreated", dateCreated)
                .executeAndReturnGeneratedKeys()
                .mapTo<Int>()
                .one()
        }
    }

    fun createApplication(jobTitle: String, applicantId: Int, state: ApplicationState, dateCreated: Instant): Int {
        return jdbi.withHandleUnchecked {
            it.createUpdate(
                """INSERT INTO application(applicantId, jobTitle, state, dateCreated)
        VALUES (:applicantId, :jobTitle, :state, :dateCreated)"""
            )
                .bind("applicantId", applicantId)
                .bind("jobTitle", jobTitle)
                .bind("state", state)
                .bind("dateCreated", dateCreated)
                .executeAndReturnGeneratedKeys()
                .mapTo<Int>()
                .one()
        }
    }
}

data class ApplicationWithApplicantsEntity(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val jobTitle: String,
    val state: ApplicationState,
    val dateCreated: Instant
)