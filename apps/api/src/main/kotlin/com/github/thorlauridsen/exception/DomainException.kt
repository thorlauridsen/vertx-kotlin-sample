package com.github.thorlauridsen.exception

import java.util.UUID
import io.netty.handler.codec.http.HttpResponseStatus

/**
 * Abstract class representing a domain exception.
 * @param message Description of the domain error.
 * @param httpStatus [HttpResponseStatus] related to the domain error.
 */
abstract class DomainException(
    override val message: String,
    val httpStatus: HttpResponseStatus,
) : Exception()

/**
 * Exception for when a customer could not be found given an id.
 * @param id [UUID]
 */
class CustomerNotFoundException(id: UUID) : DomainException(
    message = "Customer not found with id: $id",
    httpStatus = HttpResponseStatus.NOT_FOUND,
)
