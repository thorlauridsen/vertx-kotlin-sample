package com.github.thorlauridsen.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.thorlauridsen.model.CustomerInput

/**
 * Data transfer object used to create a new customer.
 * @param mail Mail address of customer.
 */
data class CustomerInputDto(
    @JsonProperty("mail")
    val mail: String,
) {

    /**
     * Convert a [CustomerInputDto] to [CustomerInput].
     * @return [CustomerInput]
     */
    fun toModel(): CustomerInput {
        return CustomerInput(
            mail = mail
        )
    }
}
