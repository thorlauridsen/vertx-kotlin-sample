package com.github.thorlauridsen.config

import io.vertx.core.Vertx
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.sqlclient.PoolOptions
import io.vertx.jdbcclient.JDBCPool
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import java.sql.DriverManager

class DatabaseInitializer(private val vertx: Vertx) {

    fun initialize(): JDBCPool {
        val jdbcUrl = "jdbc:h2:~/test"
        val username = "sa"
        val password = ""
        val liquibaseChangelog = "db/changelog/db.changelog-master.yaml"

        val connection = DriverManager.getConnection(jdbcUrl, username, password)
        val liquibase = Liquibase(
            liquibaseChangelog,
            ClassLoaderResourceAccessor(),
            JdbcConnection(connection)
        )
        liquibase.update("")

        return JDBCPool.pool(
            vertx,
            JDBCConnectOptions()
                .setJdbcUrl(jdbcUrl)
                .setUser(username)
                .setPassword(password),
            PoolOptions()
                .setMaxSize(16)
                .setName("pool-name")
        )
    }
}
