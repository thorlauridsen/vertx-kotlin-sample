package com.github.thorlauridsen

import com.github.thorlauridsen.config.DatabaseInitializer
import com.github.thorlauridsen.persistence.CustomerRepo
import com.github.thorlauridsen.route.CustomerRouter.setupCustomerRouter
import com.github.thorlauridsen.service.CustomerService
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.openapi.RouterBuilder
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

        val database = DatabaseInitializer(vertx)
        val pool = database.initialize()

        val router = Router.router(vertx)
        val customerRepo = CustomerRepo(pool)
        val customerService = CustomerService(customerRepo)

        coroutineRouter {
            setupCustomerRouter(router, customerService)
        }

        router.route("/static/*").handler(StaticHandler.create("webroot"))
        router.get("/swagger").handler { ctx ->
            ctx.response()
                .sendFile("webroot/swagger-ui.html")
        }
        router.get("/openapi.yaml").handler { ctx ->
            vertx.fileSystem().readFile("src/main/resources/openapi.yaml").onSuccess { buffer ->
                ctx.response()
                    .putHeader("content-type", "text/yaml")
                    .end(buffer)
            }.onFailure { err ->
                ctx.fail(err)
            }
        }
        vertx
            .createHttpServer()
            .requestHandler { request -> router.handle(request) }
            .listen(8080)
            .coAwait()
    }
}
