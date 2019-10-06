package com.phauer.krecruiter

import com.phauer.krecruiter.common.ApplicantEntity
import com.phauer.krecruiter.common.ApplicationEntity
import com.phauer.krecruiter.initializer.ApplicantInitializerDAO
import com.phauer.krecruiter.initializer.ApplicationInitializerDAO
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.jdbi.v3.sqlobject.statement.SqlUpdate

class TestDAO(
    jdbi: Jdbi
) {
    private val applicationDao: ApplicationInitializerDAO = jdbi.onDemand()
    private val applicantDao: ApplicantInitializerDAO = jdbi.onDemand()
    private val helperDao: TestDAOHelper = jdbi.onDemand()

    fun recreateSchema() {
        applicationDao.dropTable()
        applicantDao.dropTable()

        applicantDao.createTable()
        applicationDao.createTable()
    }

    fun clearTables() {
        helperDao.truncateTables()
    }

    fun insert(vararg applicantEntities: ApplicantEntity) {
        applicantDao.insert(applicantEntities.toList())
    }

    fun insert(vararg applicationEntities: ApplicationEntity) {
        applicationDao.insert(applicationEntities.toList())
    }

}

private interface TestDAOHelper : SqlObject {
    @SqlUpdate("TRUNCATE TABLE application, applicant RESTART IDENTITY")
    fun truncateTables()
}