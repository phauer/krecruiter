package com.phauer.krecruiter

import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import java.net.InetSocketAddress
import java.net.Socket
import javax.sql.DataSource

object PostgreSQLInstance {

    val jdbi by lazy { Jdbi.create(createDataSource()).installPlugins() }

    private fun createDataSource(): DataSource = if (isRunningLocally("localhost", 6000)) {
        PGSimpleDataSource().apply {
            setUrl("jdbc:postgresql://localhost:6000/krecruiter")
            user = "user"
            password = "password"
        }
    } else {
        val db = KPostgreSQLContainer("postgres:11.2-alpine")
        db.start()
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