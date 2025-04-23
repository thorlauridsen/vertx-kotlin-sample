package com.github.thorlauridsen.config

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.json.jackson.DatabindCodec

/**
 * Jackson configuration for JSON serialization/deserialization.
 * This class is responsible for configuring Jackson to use the Kotlin module and Java time module.
 */
object JacksonConfig {

    /**
     * Configure Jackson for JSON serialization/deserialization.
     * This method should be called at the start of the application.
     *
     * This will register the Kotlin module and Java time module.
     * It will also disable the serialization of dates as timestamps.
     * This means that dates will be serialized as ISO-8601 strings instead of timestamp numbers.
     */
    fun configureJackson() {
        val mapper = DatabindCodec.mapper()
        mapper.registerKotlinModule()
        mapper.registerModule(JavaTimeModule())
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}
