package com.phauer.recruitingapp.schemaCreation

import com.github.javafaker.Faker
import com.phauer.recruitingapp.common.ApplicationEntity
import com.phauer.recruitingapp.common.ApplicationState
import com.phauer.recruitingapp.common.logger
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class SchemaCreator(
    private val dao: SchemaCreatorDAO
) : ApplicationRunner {
    private val log by logger()
    private val faker = Faker()

    override fun run(args: ApplicationArguments) {
        log.info("Start Schema Creation...")
        dao.createTable()
        dao.truncateTable()
        val applications = generateApplications()
        dao.insert(applications)
        log.info("Finished Schema Creation.")
    }

    private fun generateApplications() = (0..999).map {
        ApplicationEntity(
            id = it,
            name = faker.name().fullName(),
            jobTitle = faker.job().title(),
            status = ApplicationState.values().random(),
            dateCreated = faker.date().past(4000, TimeUnit.DAYS).toInstant()
        )
    }
}