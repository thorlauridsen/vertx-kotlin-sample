package com.github.thorlauridsen

import com.github.thorlauridsen.config.DatabaseInitializer
import com.github.thorlauridsen.config.JacksonConfig
import com.github.thorlauridsen.persistence.CustomerRepo
import com.github.thorlauridsen.route.CustomerRouter.setupCustomerRouter
import com.github.thorlauridsen.route.SwaggerRouter.setupStaticRouter
import com.github.thorlauridsen.service.CustomerService
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.coroutineRouter

/**
 * Main verticle for the Vert.x application.
 *
 * This verticle is responsible for:
 * - Initializing the database.
 * - Setting up the HTTP routes/endpoints.
 * - Setting up the swagger documentation.
 * - Starting the HTTP server.
 */
class MainVerticle : CoroutineVerticle() {

    /**
     * Start the verticle.
     * This method is called when the verticle is deployed.
     */
    override suspend fun start() {
        JacksonConfig.configureJackson()

        val database = DatabaseInitializer(vertx)
        val pool = database.initialize()

        val router = Router.router(vertx)
        val customerRepo = CustomerRepo(pool)
        val customerService = CustomerService(customerRepo)

        coroutineRouter {
            setupCustomerRouter(router, customerService)
            setupStaticRouter(router, vertx)
        }
        vertx
            .createHttpServer()
            .requestHandler { request -> router.handle(request) }
            .listen(8080)
            .coAwait()
    }
}
