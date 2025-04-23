package com.github.thorlauridsen

import com.github.thorlauridsen.config.DatabaseInitializer
import com.github.thorlauridsen.model.CustomerInput
import com.github.thorlauridsen.persistence.CustomerRepo
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(VertxExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomerRepoTest {

    private lateinit var vertx: Vertx
    private lateinit var customerRepo: CustomerRepo

    @BeforeAll
    fun setup(vertx: Vertx) {
        this.vertx = vertx

        runTest {
            val database = DatabaseInitializer(vertx)
            val pool = database.initialize()
            customerRepo = CustomerRepo(pool)
        }
    }

    @AfterAll
    fun tearDown() {
        vertx.close()
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

            val savedCustomer = customerRepo.save(customer)

            assertNotNull(savedCustomer)
            assertNotNull(savedCustomer.id)
            assertEquals(mail, savedCustomer.mail)

            val foundCustomer = customerRepo.find(savedCustomer.id)

            assertNotNull(foundCustomer)
            assertEquals(savedCustomer.id, foundCustomer?.id)
            assertEquals(mail, foundCustomer?.mail)
        }
    }

    @Test
    fun `get customer - non-existent id - returns null`() {
        runTest {
            val id = UUID.randomUUID()
            val customer = customerRepo.find(id)
            assertNull(customer)
        }
    }
}
