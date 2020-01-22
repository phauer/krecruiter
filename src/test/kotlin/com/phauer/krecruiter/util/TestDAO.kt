package com.phauer.krecruiter.util

import com.phauer.krecruiter.common.ApplicantEntity
import com.phauer.krecruiter.common.ApplicationEntity
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlBatch
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface TestDAO : SqlObject {
    @SqlUpdate("TRUNCATE TABLE application, applicant RESTART IDENTITY")
    fun clearTables()

    @SqlQuery(
        """
        SELECT id, applicantId, jobTitle, state, dateCreated 
        FROM application
        LIMIT 1
        """
    )
    fun findOneApplication(): ApplicationEntity?

    @SqlQuery(
        """
        SELECT id, firstName, lastName, street, city, dateCreated 
        FROM applicant
        LIMIT 1
        """
    )
    fun findOneApplicant(): ApplicantEntity?

    @SqlBatch(
        """INSERT INTO applicant(id, firstName, lastName, street, city, dateCreated)
            VALUES (:id, :firstName, :lastName, :street, :city, :dateCreated)"""
    )
    fun insert(@BindBean vararg applicants: ApplicantEntity)

    @SqlBatch(
        """INSERT INTO application(id, applicantId, jobTitle, state, dateCreated)
            VALUES (:id, :applicantId, :jobTitle, :state, :dateCreated)"""
    )
    fun insert(@BindBean vararg applications: ApplicationEntity)
}
