package com.github.thorlauridsen.persistence

import com.github.thorlauridsen.model.Customer
import com.github.thorlauridsen.model.CustomerInput
import com.github.thorlauridsen.model.ICustomerRepo
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import java.util.UUID

class CustomerRepo(private val client: SqlClient) : ICustomerRepo {

    override suspend fun save(customer: CustomerInput): Customer {
        val id = UUID.randomUUID()

        client.preparedQuery("INSERT INTO customer(id, mail) VALUES ($1, $2)")
            .execute(Tuple.of(id, customer.mail))
            .coAwait()

        return find(id) ?: error("Failed to insert customer with mail: ${customer.mail}")
    }

    override suspend fun find(id: UUID): Customer? {
        val row = client
            .preparedQuery("SELECT id, mail FROM customer WHERE id = $1")
            .execute(Tuple.of(id))
            .coAwait()
            .firstOrNull()

        return row?.let { mapRowToCustomer(it) }
    }

    private fun mapRowToCustomer(row: Row): Customer {
        return Customer(
            id = row.getUUID("ID"),
            mail = row.getString("MAIL")
        )
    }
}
