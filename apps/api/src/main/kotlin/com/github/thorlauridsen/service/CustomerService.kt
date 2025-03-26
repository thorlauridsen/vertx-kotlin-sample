package com.github.thorlauridsen.service

import com.github.thorlauridsen.model.Customer
import com.github.thorlauridsen.model.CustomerInput
import com.github.thorlauridsen.model.ICustomerRepo
import io.vertx.core.impl.logging.LoggerFactory
import java.util.UUID
import kotlin.jvm.java

/**
 * This service is responsible for:
 * - Saving customers.
 * - Fetching customers.
 *
 * @param customerRepo [ICustomerRepo] to interact with the database.
 */
class CustomerService(private val customerRepo: ICustomerRepo) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Save a customer.
     * @param customer [CustomerInput] to save.
     * @return [Customer] retrieved from database.
     */
    suspend fun save(customer: CustomerInput): Customer {
        logger.info("Saving customer to database: $customer")

        return customerRepo.save(customer)
    }

    /**
     * Get a customer given an id.
     * @param id [UUID] to fetch customer.
     * @throws CustomerNotFoundException if no customer found with given id.
     * @return [Customer] retrieved from database.
     */
    suspend fun find(id: UUID): Customer {
        logger.info("Retrieving customer with id: $id")

        val customer = customerRepo.find(id)
            ?: error("Customer with id: $id not found")

        logger.info("Found customer: $customer")
        return customer
    }
}
