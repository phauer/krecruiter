package com.phauer.krecruiter

import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import java.net.InetSocketAddress
import java.net.Socket


private const val localHost = "localhost"
private const val localPort = 6000

object PostgreSQLInstance {

    val dataSource by lazy {
        if (isRunningLocally()) {
            PGSimpleDataSource().apply {
                setUrl("jdbc:postgresql://${localHost}:${localPort}/krecruiter")
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
    }

    val jdbi by lazy { Jdbi.create(dataSource).installPlugins() }

    private fun isRunningLocally() = try {
        val socket = Socket()
        socket.connect(InetSocketAddress(localHost, localPort), 100)
        socket.close()
        true
    } catch (exception: Exception) {
        false
    }
}

class KPostgreSQLContainer(imageName: String) : PostgreSQLContainer<KPostgreSQLContainer>(imageName)