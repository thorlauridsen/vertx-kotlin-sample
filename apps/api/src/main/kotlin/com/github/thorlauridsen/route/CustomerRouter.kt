package com.github.thorlauridsen.route

import com.github.thorlauridsen.dto.CustomerInputDto
import com.github.thorlauridsen.dto.toDto
import com.github.thorlauridsen.service.CustomerService
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.CoroutineRouterSupport
import java.util.UUID

/**
 * Customer router for defining the routes related to customer operations.
 */
object CustomerRouter {

    /**
     * Sets up the HTTP endpoints for customer operations.
     *
     * @param router The router to set up.
     * @param customerService The customer service to handle customer operations.
     */
    fun CoroutineRouterSupport.setupCustomerRouter(router: Router, customerService: CustomerService) {

        router.get("/customers/:id")
            .produces("application/json")
            .coHandler { routingContext ->
                val id = routingContext.request().getParam("id")
                val uuid = UUID.fromString(id)
                val customer = customerService.find(uuid)
                val json = JsonObject.mapFrom(customer).encode()

                routingContext.response()
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

                routingContext.response()
                    .putHeader("content-type", "application/json")
                    .end(json)
            }
    }
}
