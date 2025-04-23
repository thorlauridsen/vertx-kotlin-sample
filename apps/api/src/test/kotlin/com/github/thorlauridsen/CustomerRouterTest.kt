package com.github.thorlauridsen

import com.github.thorlauridsen.dto.CustomerInputDto
import io.netty.handler.codec.http.HttpResponseStatus
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
class CustomerRouterTest {

    private val clientOptions = WebClientOptions().setDefaultHost("localhost").setDefaultPort(8080)

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
    fun `get customer - random id - returns not found`(vertx: Vertx, testContext: VertxTestContext) {
        val client = WebClient.create(vertx, clientOptions)
        val id = UUID.randomUUID()

        client.get("/customers/$id").send { ar ->
            if (ar.succeeded()) {
                val response = ar.result()
                testContext.verify {
                    assertEquals(HttpResponseStatus.NOT_FOUND.code(), response.statusCode())
                    testContext.completeNow()
                }
            } else {
                testContext.failNow(ar.cause())
            }
        }
    }

    @Test
    fun `post customer - get customer - success`(vertx: Vertx, testContext: VertxTestContext) {
        val client = WebClient.create(vertx, clientOptions)
        val customer = CustomerInputDto(mail = "test@example.com")
        val customerJson = JsonObject.mapFrom(customer)
        var customerId: String? = null

        client.post("/customers")
            .sendJsonObject(customerJson)
            .compose { postRes ->
                testContext.verify {
                    assertEquals(HttpResponseStatus.OK.code(), postRes.statusCode())
                    assertEquals(customer.mail, postRes.bodyAsJsonObject().getString("mail"))
                }
                customerId = postRes.bodyAsJsonObject().getString("id")
                client.get("/customers/$customerId").send()
            }
            .onSuccess { getRes ->
                testContext.verify {
                    assertEquals(HttpResponseStatus.OK.code(), getRes.statusCode())
                    val json = getRes.bodyAsJsonObject()
                    assertEquals(customerId, json.getString("id"))
                    assertEquals(customer.mail, json.getString("mail"))
                }
                testContext.completeNow()
            }
    }
}
