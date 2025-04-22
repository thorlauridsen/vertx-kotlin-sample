package com.github.thorlauridsen.route

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler

/**
 * Static router for defining the routes related to Swagger/OpenAPI documentation.
 */
object SwaggerRouter {

    /**
     * Sets up the HTTP endpoints for static files for Swagger/OpenAPI documentation.
     *
     * @param router The router to set up.
     * @param vertx The Vertx instance to access the file system.
     */
    fun setupStaticRouter(router: Router, vertx: Vertx) {
        router.route("/static/*").handler(StaticHandler.create("webroot"))

        router.get("/swagger").handler { ctx ->
            ctx.response().sendFile("webroot/swagger-ui.html")
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
    }
}
