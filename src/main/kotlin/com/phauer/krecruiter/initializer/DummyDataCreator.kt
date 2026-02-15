package com.phauer.krecruiter.initializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.javafaker.Faker
import com.phauer.krecruiter.common.ApplicantEntity
import com.phauer.krecruiter.common.ApplicationEntity
import com.phauer.krecruiter.common.ApplicationState
import com.phauer.krecruiter.common.logger
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlBatch
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class SchemaInitializer(
    jdbi: Jdbi,
    private val mapper: ObjectMapper
) : ApplicationRunner {
    private val log by logger()
    private val faker = Faker()
    private val applicantAmount = 10
    private val dao = jdbi.onDemand(DummyDataCreatorDAO::class.java)

    override fun run(args: ApplicationArguments) {
        log.info("Start Dummy Data Creation...")

        dao.clearTables()

        val applicants = generateApplicants()
        dao.insertApplicants(applicants)

        val applications = generateApplications(applicants)
        dao.insertApplications(applications)

        log.info("Finished Dummy Data  Creation.")
    }

    private fun generateApplicants() = (0..applicantAmount).map {
        ApplicantEntity(
            id = it * 100000,
            firstName = faker.name().firstName(),
            lastName = faker.name().lastName(),
            street = faker.address().streetAddress(),
            city = faker.address().city(),
            dateCreated = faker.date().past(4000, TimeUnit.DAYS).toInstant()
        )
    }

    private fun generateApplications(applicants: List<ApplicantEntity>) = (0..applicantAmount + 150).map {
        ApplicationEntity(
            id = it * 100000,
            applicantId = applicants.random().id,
            jobTitle = faker.job().title(),
            state = ApplicationState.values().random(),
            dateCreated = faker.date().past(4000, TimeUnit.DAYS).toInstant(),
            attachments = if (faker.bool().bool()) {
                val attachmentMap = (0..faker.number().numberBetween(1, 4)).associate {
                    faker.lorem().sentence(1) to faker.file().fileName()
                }
                mapper.writeValueAsString(attachmentMap)
            } else {
                null
            }
        )
    }

}

private interface DummyDataCreatorDAO : SqlObject {
    @SqlBatch(
        """INSERT INTO applicant(id, firstName, lastName, street, city, dateCreated)
        VALUES (:id, :firstName, :lastName, :street, :city, :dateCreated)"""
    )
    fun insertApplicants(@BindBean applicants: List<ApplicantEntity>)

    @SqlBatch(
        """INSERT INTO application(id, applicantId, jobTitle, state, attachments, dateCreated)
        VALUES (:id, :applicantId, :jobTitle, :state, :attachments, :dateCreated)"""
    )
    fun insertApplications(@BindBean applications: List<ApplicationEntity>)

    @SqlUpdate("TRUNCATE TABLE application, applicant RESTART IDENTITY")
    fun clearTables()
}

