package com.phauer.krecruiter.initializer

import com.phauer.krecruiter.common.ApplicationEntity
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlBatch
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface ApplicationInitializerDAO : SqlObject {

    @SqlUpdate(
        """DROP TABLE IF EXISTS application"""
    )
    fun dropTable()

    @SqlUpdate(
        """CREATE TABLE application (
            id SERIAL PRIMARY KEY,
            applicantId INTEGER REFERENCES applicant(id),
            jobTitle VARCHAR(120),
            status VARCHAR(50),
            dateCreated TIMESTAMP
            )"""
    )
    fun createTable()

    @SqlBatch(
        """INSERT INTO application(id, applicantId, jobTitle, status, dateCreated)
        VALUES (:id, :applicantId, :jobTitle, :status, :dateCreated)"""
    )
    fun insert(@BindBean applications: List<ApplicationEntity>)
}