package com.phauer.recruitingapp.addressesApi

import com.phauer.recruitingapp.applicationView.ApplicationDAO
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AddressesController(
    private val dao: ApplicationDAO,
    private val client: AddressValidationClient
) {
    @GetMapping("/addresses")
    fun getAddresses(): List<AddressResponseDTO> {

        // TODO get from DAO
        val address = AddressValidationRequestDTO(
            street = "Karl-Liebknecht-Straße 12",
            city = "Leipzig",
            zipCode = "04111"
        )

        val response = client.validateAddress(address)

        println(response)
        println(response)
        println(response)

        // when status -> rejected

        // call address validation service

        // TODO bei api besonderheit: remote call! when + sealed classes
        // top-level, ext func

        // return DTO
        return listOf(
            AddressResponseDTO("asdfasdf22")
        )
    }
}

data class AddressResponseDTO(
    val bla: String
)