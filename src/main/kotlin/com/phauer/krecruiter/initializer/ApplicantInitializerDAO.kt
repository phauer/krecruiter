package com.phauer.krecruiter.initializer

import com.phauer.krecruiter.common.ApplicantEntity
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlBatch
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface ApplicantInitializerDAO : SqlObject {

    @SqlUpdate(
        """DROP TABLE IF EXISTS applicant"""
    )
    fun dropTable()

    @SqlUpdate(
        """CREATE TABLE applicant (
            id SERIAL PRIMARY KEY,
            firstName VARCHAR(60),
            lastName VARCHAR(60),
            street VARCHAR(60),
            city VARCHAR(30),
            dateCreated TIMESTAMP
            )"""
    )
    fun createTable()

    @SqlBatch(
        """INSERT INTO applicant(id, firstName, lastName, street, city, dateCreated)
        VALUES (:id, :firstName, :lastName, :street, :city, :dateCreated)"""
    )
    fun insert(@BindBean applicants: List<ApplicantEntity>)
}