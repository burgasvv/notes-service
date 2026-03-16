package org.burgas.database

import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import redis.clients.jedis.Jedis

class DatabaseFactory {

    companion object {

        val config: ApplicationConfig = ApplicationConfig("application.yaml")

        val postgres: Database = Database.connect(
            driver = config.property("ktor.postgres.driver").getString(),
            user = config.property("ktor.postgres.user").getString(),
            password = config.property("ktor.postgres.password").getString(),
            url = config.property("ktor.postgres.url").getString()
        )

        val redis = Jedis(
            config.property("ktor.redis.host").getString(),
            config.property("ktor.redis.port").getString().toInt()
        )
    }
}