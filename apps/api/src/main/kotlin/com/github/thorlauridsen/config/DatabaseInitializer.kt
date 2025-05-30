package com.github.thorlauridsen.config

import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.Vertx
import io.vertx.core.internal.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.jdbcclient.JDBCPool
import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PoolOptions
import java.sql.DriverManager
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

/**
 * This class is responsible for initializing the database.
 * It creates a connection pool and runs Liquibase migrations.
 *
 * @param vertx [Vertx] instance to create the connection pool.
 */
class DatabaseInitializer(private val vertx: Vertx) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Initializes the database connection pool.
     *
     * This method loads the database configuration from the application.yaml file,
     * runs Liquibase migrations, and returns a configured [Pool] instance.
     * The jdbcUrl is determined based on the database type.
     * The jdbcUrl is required for Liquibase to run migrations.
     *
     * @return a [Pool] instance for the database connection.
     */
    suspend fun initialize(): Pool {
        logger.info("Initializing database...")

        val jsonConfig = loadConfiguration()
        val database = DatabaseConfig.getConfig(jsonConfig)

        val jdbcUrl = if (database.isH2()) {
            "jdbc:h2:~/test"
        } else {
            "jdbc:postgresql://${database.host}:${database.port}/${database.name}"
        }

        runLiquibase(jdbcUrl = jdbcUrl, database = database)

        return if (database.isH2()) {
            logger.info("Connecting to in-memory H2 database...")
            getJdbcPool(jdbcUrl = jdbcUrl, database = database)

        } else {
            logger.info("Connecting to PostgreSQL...")
            getPgPool(database)
        }
    }

    /**
     * Creates a JDBC pool for H2 or other JDBC-compatible databases.
     *
     * @param jdbcUrl the JDBC URL of the database.
     * @param database the database configuration containing username and password.
     * @return a configured [Pool] instance for JDBC.
     */
    private fun getJdbcPool(
        jdbcUrl: String,
        database: DatabaseConfig,
    ): Pool {
        val poolOptions = PoolOptions()
            .setMaxSize(10)
            .setName("database-pool")

        val connectOptions = JDBCConnectOptions()
            .setJdbcUrl(jdbcUrl)
            .setUser(database.username)
            .setPassword(database.password)

        return JDBCPool.pool(
            vertx,
            connectOptions,
            poolOptions
        )
    }

    /**
     * Creates a non-blocking PgPool for PostgreSQL with high concurrency settings.
     *
     * @param database the database configuration containing connection details.
     * @return a configured PostgreSQL [Pool] instance.
     */
    private fun getPgPool(database: DatabaseConfig): Pool {

        val poolOptions = PoolOptions()
            .setMaxSize(30)
            .setName("database-pool")

        val pgConnectOptions = PgConnectOptions()
            .setHost(database.host)
            .setPort(database.port)
            .setDatabase(database.name)
            .setUser(database.username)
            .setPassword(database.password)
            .setCachePreparedStatements(true)
            .setPreparedStatementCacheMaxSize(256)
            .setPreparedStatementCacheSqlLimit(2048)

        return PgBuilder
            .pool()
            .with(poolOptions)
            .connectingTo(pgConnectOptions)
            .using(vertx)
            .build()
    }

    /**
     * Runs Liquibase migrations in a dispatcher designed for blocking operations.
     * Liquibase requires a blocking context to execute database migrations.
     *
     * @param jdbcUrl the JDBC URL of the database.
     * @param database the database configuration containing connection details.
     */
    private suspend fun runLiquibase(
        jdbcUrl: String,
        database: DatabaseConfig,
    ) = withContext(Dispatchers.IO) {

        logger.info("Running Liquibase migrations from ${database.liquibaseChangelog}")
        val connection = DriverManager.getConnection(jdbcUrl, database.username, database.password)

        val liquibase = Liquibase(
            database.liquibaseChangelog,
            ClassLoaderResourceAccessor(),
            JdbcConnection(connection)
        )
        liquibase.update("")

        logger.info("Liquibase migrations completed")
    }

    /**
     * Load configuration from the application.yaml file.
     *
     * This will first load the configuration from the YAML file and then override it with system properties.
     * It is important to note that the system properties must match the keys in the YAML file.
     * For example, if the YAML file has a key "database.host", then the system property must be "database.host".
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

        retriever.config
            .onSuccess {
                continuation.resume(it)
            }.onFailure {
                continuation.resumeWithException(it)
            }
    }
}
