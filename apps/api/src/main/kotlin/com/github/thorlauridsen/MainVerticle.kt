package com.github.thorlauridsen

import com.github.thorlauridsen.config.DatabaseInitializer
import com.github.thorlauridsen.dto.CustomerInputDto
import com.github.thorlauridsen.dto.toDto
import com.github.thorlauridsen.persistence.CustomerRepo
import com.github.thorlauridsen.service.CustomerService
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.coroutineRouter
import java.util.UUID

class MainVerticle : CoroutineVerticle() {

    override suspend fun start() {

        val database = DatabaseInitializer(vertx)
        val pool = database.initialize()

        val router = Router.router(vertx)
        val customerRepo = CustomerRepo(pool)
        val customerService = CustomerService(customerRepo)

        coroutineRouter {
            router.get("/customers/:id")
                .produces("application/json")
                .coHandler { routingContext ->
                    val id = routingContext.request().getParam("id")
                    val uuid = UUID.fromString(id)
                    val customer = customerService.find(uuid)
                    val json = JsonObject.mapFrom(customer).encode()
                    routingContext
                        .response()
                        .putHeader("content-type", "application/json")
                        .end(json)
                }
            router.post("/customers")
                .consumes("application/json")
                .produces("application/json")
                .handler(BodyHandler.create())
                .coHandler { routingContext ->
                    val body = routingContext.body()
                    val customer = body.asJsonObject().mapTo(CustomerInputDto::class.java)
                    val saved = customerService.save(customer.toModel())
                    val json = JsonObject.mapFrom(saved.toDto()).encode()
                    routingContext
                        .response()
                        .putHeader("content-type", "application/json")
                        .end(json)
                }
        }
        vertx
            .createHttpServer()
            .requestHandler { request -> router.handle(request) }
            .listen(8080)
            .coAwait()
    }
}
