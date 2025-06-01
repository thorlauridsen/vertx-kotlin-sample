package com.github.thorlauridsen.config

import io.vertx.core.json.JsonObject

/**
 * Configuration for the database connection.
 *
 * @property host The hostname or IP address of the database server.
 * @property type The type of the database, such as H2 or PostgreSQL.
 * @property port The port number on which the database server is listening.
 * @property name The name of the database to connect to.
 * @property username The username for authenticating with the database.
 * @property password The password for authenticating with the database.
 * @property liquibaseChangelog The path to the Liquibase changelog file for database migrations.
 */
data class DatabaseConfig(
    val host: String,
    val type: DatabaseType,
    val port: Int,
    val name: String,
    val username: String,
    val password: String,
    val liquibaseChangelog: String,
) {

    /**
     * Checks if the database type is H2.
     *
     * @return true if the database type is H2, false otherwise.
     */
    fun isH2(): Boolean {
        return type == DatabaseType.H2
    }

    companion object {

        /**
         * Parses the database configuration from a JSON object.
         *
         * @param jsonObject The JSON object containing the database configuration.
         * @return A [DatabaseConfig] instance with the parsed configuration.
         * @throws IllegalArgumentException if any required field is missing or null.
         */
        fun getConfig(jsonObject: JsonObject): DatabaseConfig {

            val database = jsonObject.getJsonObject("database")
            requireNotNull(database) { "'database' cannot be null in application.yaml" }

            val host = database.getString("host")
            val port = database.getInteger("port")
            val name = database.getString("name")
            val username = database.getString("username")
            val password = database.getString("password")
            val liquibaseChangelog = database.getJsonObject("liquibase").getString("changelog")

            requireNotNull(host) { "'database.host' cannot be null in application.yaml" }
            requireNotNull(port) { "'database.port' cannot be null in application.yaml" }
            requireNotNull(name) { "'database.name' cannot be null in application.yaml" }
            requireNotNull(username) { "'database.username' cannot be null in application.yaml" }
            requireNotNull(password) { "'database.password' cannot be null in application.yaml" }
            requireNotNull(liquibaseChangelog) { "'database.liquibase.changelog' cannot be null in application.yaml" }

            val type = if (host.lowercase().contains("h2")) {
                DatabaseType.H2
            } else {
                DatabaseType.Postgres
            }

            return DatabaseConfig(
                host = host,
                type = type,
                port = port,
                name = name,
                username = username,
                password = password,
                liquibaseChangelog = liquibaseChangelog
            )
        }
    }
}
