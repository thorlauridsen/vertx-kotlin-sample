package com.github.thorlauridsen.route

import com.github.thorlauridsen.dto.ErrorDto
import com.github.thorlauridsen.exception.DomainException
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.impl.logging.Logger
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

/**
 * Abstract base class for handling router requests.
 * This class provides a method to handle exceptions during request processing.
 */
abstract class BaseRouter {

    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Handles exceptions that occur during the processing of a request.
     *
     * This is a higher-order function that takes a block of code to execute.
     * It catches DomainException and returns an appropriate error response.
     * It also catches any other exceptions and returns a generic error response.
     *
     * @param block The block of code to execute, which may throw exceptions.
     */
    suspend fun RoutingContext.handleRequest(
        block: suspend (RoutingContext) -> Unit,
    ) {
        try {
            block(this)
        } catch (ex: DomainException) {
            logger.error("Domain exception occurred: ${ex.message}", ex)

            val errorDto = ErrorDto(ex.message)
            val errorJson = JsonObject.mapFrom(errorDto).encode()

            response()
                .setStatusCode(ex.httpStatus.code())
                .putHeader("content-type", "application/json")
                .end(errorJson)

        } catch (ex: Exception) {
            logger.error("Unexpected exception occurred: ${ex.message}", ex)

            val errorDto = ErrorDto(ex.message ?: "Internal Server Error")
            val errorJson = JsonObject.mapFrom(errorDto).encode()

            response()
                .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                .putHeader("content-type", "application/json")
                .end(errorJson)
        }
    }
}
