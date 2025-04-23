package com.github.thorlauridsen

import com.github.thorlauridsen.config.DatabaseInitializer
import com.github.thorlauridsen.exception.CustomerNotFoundException
import com.github.thorlauridsen.model.CustomerInput
import com.github.thorlauridsen.persistence.CustomerRepo
import com.github.thorlauridsen.service.CustomerService
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(VertxExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomerServiceTest {

    private lateinit var vertx: Vertx
    private lateinit var customerService: CustomerService

    @BeforeAll
    fun setup(vertx: Vertx) {
        this.vertx = vertx

        runTest {
            val database = DatabaseInitializer(vertx)
            val pool = database.initialize()
            val customerRepo = CustomerRepo(pool)
            customerService = CustomerService(customerRepo)
        }
    }

    @AfterAll
    fun tearDown() {
        vertx.close()
    }

    @Test
    fun `get customer - random id - returns not found`() {
        runTest {
            val id = UUID.randomUUID()

            assertThrows<CustomerNotFoundException> {
                customerService.find(id)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "alice@gmail.com",
            "bob@gmail.com",
        ]
    )
    fun `save customer - get customer - success`(mail: String) {
        runTest {
            val customer = CustomerInput(mail)
            val saved = customerService.save(customer)

            assertNotNull(saved.id)
            assertEquals(customer.mail, saved.mail)

            val retrieved = customerService.find(saved.id)
            assertEquals(saved.id, retrieved.id)
            assertEquals(customer.mail, retrieved.mail)
        }
    }
}
