package com.github.thorlauridsen

import com.github.thorlauridsen.dto.CustomerInputDto
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class IntegrationTest {

    @BeforeEach
    fun deployVerticle(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(MainVerticle()) { ar ->
            if (ar.succeeded()) {
                testContext.completeNow()
            } else {
                testContext.failNow(ar.cause())
            }
        }
    }

    @Test
    fun testHttpEndpoint(vertx: Vertx, testContext: VertxTestContext) {
        val clientOptions = WebClientOptions().setDefaultHost("localhost").setDefaultPort(8080)
        val client = WebClient.create(vertx, clientOptions)
        val id = UUID.randomUUID()

        client.get("/customers/$id").send { ar ->
            if (ar.succeeded()) {
                val response = ar.result()
                testContext.verify {
                    assertEquals(500, response.statusCode())
                    testContext.completeNow()
                }
            } else {
                testContext.failNow(ar.cause())
            }
        }
    }

    @Test
    fun postCustomer(vertx: Vertx, testContext: VertxTestContext) {
        val clientOptions = WebClientOptions().setDefaultHost("localhost").setDefaultPort(8080)
        val client = WebClient.create(vertx, clientOptions)

        val customer = CustomerInputDto(mail = "test@example.com")
        val customerJson = JsonObject.mapFrom(customer)

        client.post("/customers").sendJsonObject(customerJson) { ar ->
            if (ar.succeeded()) {
                val response = ar.result()
                testContext.verify {
                    assertEquals(200, response.statusCode())
                    testContext.completeNow()
                }
            } else {
                testContext.failNow(ar.cause())
            }
        }
    }
}
