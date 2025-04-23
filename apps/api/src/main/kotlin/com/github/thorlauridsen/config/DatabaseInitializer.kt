package com.github.thorlauridsen.config

import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.Vertx
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.sqlclient.PoolOptions
import io.vertx.jdbcclient.JDBCPool
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import java.sql.DriverManager
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * This class is responsible for initializing the database.
 * It creates a connection pool and runs Liquibase migrations.
 *
 * @param vertx [Vertx] instance to create the connection pool.
 */
class DatabaseInitializer(private val vertx: Vertx) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Initialize the database.
     *
     * This method creates a connection pool and runs Liquibase migrations.
     * It returns a [JDBCPool] instance.
     *
     * @return [JDBCPool] instance.
     */
    suspend fun initialize(): JDBCPool {
        val config = loadConfiguration()
        val dbConfig = config.getJsonObject("database")

        val jdbcUrl = dbConfig.getString("url")
        val username = dbConfig.getString("username")
        val password = dbConfig.getString("password")
        val liquibaseChangelog = dbConfig.getJsonObject("liquibase").getString("changelog")

        logger.info("Connecting to database with url: $jdbcUrl")
        val connection = DriverManager.getConnection(jdbcUrl, username, password)

        logger.info("Running Liquibase migrations")
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
                .setName("database-pool")
        )
    }

    /**
     * Load configuration from the application.yaml file.
     *
     * This will first load the configuration from the YAML file and then override it with system properties.
     * It is important to note that the system properties must match the keys in the YAML file.
     * For example, if the YAML file has a key "database.url", then the system property must be "database.url".
     * Vert.x will automatically convert the configuration to a JsonObject.
     *
     * @return [JsonObject] containing the configuration.
     */
    private suspend fun loadConfiguration(): JsonObject = suspendCoroutine { continuation ->
        val yamlStore = ConfigStoreOptions()
            .setType("file")
            .setFormat("yaml")
            .setConfig(JsonObject().put("path", "application.yaml"))

        val sysPropsStore = ConfigStoreOptions()
            .setType("sys")
            .setConfig(JsonObject().put("hierarchical", true))

        val options = ConfigRetrieverOptions()
            .addStore(yamlStore)
            .addStore(sysPropsStore)

        val retriever = ConfigRetriever.create(vertx, options)

        retriever.getConfig { ar ->
            if (ar.failed()) {
                continuation.resumeWithException(ar.cause())
                return@getConfig
            }
            val full = ar.result()
            continuation.resume(full)
        }
    }
}
