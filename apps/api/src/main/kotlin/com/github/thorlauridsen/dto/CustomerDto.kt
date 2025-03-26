package com.github.thorlauridsen.dto

import com.github.thorlauridsen.model.Customer
import java.util.UUID

/**
 * Data transfer object representing a customer.
 * @param id [UUID] of customer.
 * @param mail Mail address of customer.
 */
data class CustomerDto(
    val id: UUID,
    val mail: String,
)

/**
 * Convert a [Customer] to [CustomerDto].
 * @return [CustomerDto]
 */
fun Customer.toDto(): CustomerDto {
    return CustomerDto(
        id = id,
        mail = mail
    )
}
