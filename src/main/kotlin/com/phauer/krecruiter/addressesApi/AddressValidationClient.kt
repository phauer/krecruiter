package com.phauer.krecruiter.addressesApi

import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

@Component
class AddressValidationClient (
    private val template: RestTemplate
) {

    private val targetUrl = "http://localhost:5000/validateAddress"
//    private val targetUrl = "https://address-validation.herokuapp.com/validateAddress"

    fun validateAddress(request: AddressValidationRequestDTO) = try {
        template.getForObject<AddressValidationResponseDTO>("$targetUrl?address=${request.street},${request.zipCode},${request.city}")
    } catch (exception: HttpClientErrorException) {
        throw AddressValidationException("Request failed. URL: $targetUrl", exception)
    } catch (exception: HttpServerErrorException) {
        throw AddressValidationException("Request failed. URL: $targetUrl. Status code: ${exception.rawStatusCode}. Message: ${exception.message}.", exception)
    } catch (exception: RestClientException) {
        throw AddressValidationException("Request failed. URL: $targetUrl. Message: ${exception.message}", exception)
    }
}

class AddressValidationException(message: String, cause: Exception): RuntimeException(message, cause)

data class AddressValidationRequestDTO(
    val street: String,
    val city: String,
    val zipCode: String
)

data class AddressValidationResponseDTO(
    val address: String,
    val isValid: Boolean
)