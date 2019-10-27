package com.phauer.krecruiter.applicationApi

import com.fasterxml.jackson.databind.ObjectMapper
import com.phauer.krecruiter.common.Outcome
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class AddressValidationClient (
    private val client: OkHttpClient,
    private val mapper: ObjectMapper,
    @Value("\${krecruiter.address-validation-service.base-url}") private val baseUrl: String
) {
    fun validateAddress(street: String, city: String): Outcome<AddressValidationResponseDTO> {
        val request = Request.Builder()
            .get().url("$baseUrl?address=$street,$city")
            .build()
        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Outcome.Success(response.body!!.string().toObject())
            } else {
                Outcome.Error("Request failed. URL: ${request.url}. code: ${response.code}. body: ${response.body}")
            }
        } catch (exception: IOException) {
            Outcome.Error("Request failed. URL: ${request.url}", exception)
        }
    }

    private inline fun <reified T> String.toObject(): T = mapper.readValue(this, T::class.java)
}

data class AddressValidationResponseDTO(
    val valid: Boolean
)