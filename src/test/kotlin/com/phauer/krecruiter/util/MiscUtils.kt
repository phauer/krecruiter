package com.phauer.krecruiter.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType
import com.phauer.krecruiter.SpringConfiguration
import com.phauer.krecruiter.applicationApi.ApplicationDTO
import okhttp3.OkHttpClient
import java.time.Instant

object TestObjects {
    val mapper: ObjectMapper = SpringConfiguration().objectMapper()
    val httpClient: OkHttpClient = SpringConfiguration().httpClient()
    val applicationDtoListType: CollectionType = mapper.typeFactory.constructCollectionType(List::class.java, ApplicationDTO::class.java)
}

fun <T> T.toJson(): String = TestObjects.mapper.writeValueAsString(this)

fun Int.toInstant(): Instant = Instant.ofEpochSecond(this.toLong())
