package com.phauer.krecruiter.initializer

import com.github.javafaker.Faker
import com.phauer.krecruiter.common.ApplicantEntity
import com.phauer.krecruiter.common.ApplicationEntity
import com.phauer.krecruiter.common.ApplicationState
import com.phauer.krecruiter.common.logger
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class SchemaInitializer(
    private val applicationDao: ApplicationInitializerDAO,
    private val applicantDao: ApplicantInitializerDAO
) : ApplicationRunner {
    private val log by logger()
    private val faker = Faker()
    private val applicantAmount = 500

    override fun run(args: ApplicationArguments) {
        log.info("Start Schema Creation...")

        applicationDao.dropTable()
        applicantDao.dropTable()

        applicantDao.createTable()
        applicationDao.createTable()

        val applicants = generateApplicants()
        applicantDao.insert(applicants)

        val applications = generateApplications(applicants)
        applicationDao.insert(applications)

        log.info("Finished Schema Creation.")
    }

    private fun generateApplicants() = (0..applicantAmount).map {
        ApplicantEntity(
            id = it,
            firstName = faker.name().firstName(),
            lastName = faker.name().lastName(),
            street = faker.address().streetAddress(),
            city = faker.address().city(),
            dateCreated = faker.date().past(4000, TimeUnit.DAYS).toInstant()
        )
    }

    private fun generateApplications(applicants: List<ApplicantEntity>) = (0..applicantAmount + 150).map {
        ApplicationEntity(
            id = it,
            applicantId = applicants.random().id,
            jobTitle = faker.job().title(),
            status = ApplicationState.values().random(),
            dateCreated = faker.date().past(4000, TimeUnit.DAYS).toInstant()
        )
    }
}