package com.github.thorlauridsen

import com.github.thorlauridsen.dto.CustomerDto
import com.github.thorlauridsen.dto.CustomerInputDto
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import java.util.UUID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(VertxExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomerRouterTest {

    private lateinit var vertx: Vertx
    private lateinit var client: WebClient

    private val clientOptions = WebClientOptions()
        .setDefaultHost("localhost")
        .setDefaultPort(8080)

    @BeforeAll
    fun setup(vertx: Vertx, testContext: VertxTestContext) {
        this.vertx = vertx
        client = WebClient.create(vertx, clientOptions)

        vertx.deployVerticle(MainVerticle()).onSuccess {
            testContext.completeNow()
        }.onFailure { ar ->
            testContext.failNow(ar)
        }
    }

    @AfterAll
    fun tearDown() {
        vertx.close()
    }

    @Test
    fun `get customer - random id - returns not found`() {
        val id = UUID.randomUUID()

        client.get("/customers/$id").send().onSuccess {
            val response = it
            assertEquals(HttpResponseStatus.NOT_FOUND.code(), response.statusCode())
        }.onFailure { ar ->
            throw ar
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "alice@gmail.com",
            "bob@gmail.com",
        ]
    )
    fun `post customer - get customer - success`(mail: String) {
        val customer = CustomerInputDto(mail)
        val customerJson = JsonObject.mapFrom(customer)
        var customerId: String? = null

        client.post("/customers")
            .sendJsonObject(customerJson)
            .compose { postResponse ->
                assertEquals(HttpResponseStatus.CREATED.code(), postResponse.statusCode())
                assertEquals(customer.mail, postResponse.bodyAsJsonObject().getString("mail"))

                customerId = postResponse.bodyAsJsonObject().getString("id")
                client.get("/customers/$customerId").send()
            }
            .onSuccess { getResponse ->
                assertEquals(HttpResponseStatus.CREATED.code(), getResponse.statusCode())
                val json = getResponse.bodyAsJsonObject()
                val foundCustomer = json.mapTo(CustomerDto::class.java)

                assertEquals(customerId, foundCustomer.id)
                assertEquals(customer.mail, foundCustomer.mail)
            }
    }
}
