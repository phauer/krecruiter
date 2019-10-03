package com.phauer.recruitingapp.schemaCreation

import com.phauer.recruitingapp.common.ApplicationEntity
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlBatch
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface SchemaCreatorDAO : SqlObject {

    @SqlUpdate(
        """CREATE TABLE IF NOT EXISTS application (
            id SERIAL PRIMARY KEY,
            name VARCHAR(60),
            jobTitle VARCHAR(60),
            status VARCHAR(30),
            dateCreated TIMESTAMP
            )"""
    )
    fun createTable()

    @SqlUpdate(
        """TRUNCATE TABLE application"""
    )
    fun truncateTable()


    @SqlBatch(
        """INSERT INTO application(id, name, jobTitle, status, dateCreated)
        VALUES (:id, :name, :jobTitle, :status, :dateCreated)"""
    )
    fun insert(@BindBean application: List<ApplicationEntity>)
}