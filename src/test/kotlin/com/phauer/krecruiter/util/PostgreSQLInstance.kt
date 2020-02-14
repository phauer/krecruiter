package com.phauer.krecruiter.util

import org.flywaydb.core.Flyway
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import java.net.InetSocketAddress
import java.net.Socket
import javax.sql.DataSource

object PostgreSQLInstance {

    val jdbi: Jdbi by lazy {
        val dataSource = createDataSource()
        Flyway.configure().dataSource(dataSource).load().migrate()
        Jdbi.create(dataSource).installPlugins()
    }

    private fun createDataSource(): DataSource = if (isRunningLocally("localhost", 6000)) {
        PGSimpleDataSource().apply {
            setUrl("jdbc:postgresql://localhost:6000/krecruiter")
            user = "user"
            password = "password"
        }
    } else {
        val db = KPostgreSQLContainer("postgres:12.1-alpine").apply {
            // reuse: good, but still takes 2 - 3 s more than the docker-compose-based approach for shorting the turn-around time.
            // mind that you have to add "testcontainers.reuse.enable=true" to ~/.testcontainers.properties
            // consider the env var TESTCONTAINERS_RYUK_DISABLED=true for further speed up
            // withReuse(true)
            start()
        }
        PGSimpleDataSource().apply {
            setUrl(db.jdbcUrl)
            user = db.username
            password = db.password
        }
    }

    private fun isRunningLocally(host: String, port: Int) = try {
        val socket = Socket()
        socket.connect(InetSocketAddress(host, port), 100)
        socket.close()
        true
    } catch (exception: Exception) {
        false
    }
}

class KPostgreSQLContainer(imageName: String) : PostgreSQLContainer<KPostgreSQLContainer>(imageName)