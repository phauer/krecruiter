package com.phauer.krecruiter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.jdbi.v3.core.Jdbi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

@Configuration
class SpringConfiguration{

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    @Bean
    fun httpClient(): OkHttpClient {
        val dispatcher = Dispatcher().apply {
            maxRequestsPerHost = 50
            maxRequests = 200
        }
        return OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor)
            .dispatcher(dispatcher)
            .connectTimeout(10_000, TimeUnit.MILLISECONDS)
            .readTimeout(20_000, TimeUnit.MILLISECONDS)
            .writeTimeout(20_000, TimeUnit.MILLISECONDS)
            .build()
    }

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun jdbi(dataSource: DataSource) = Jdbi.create(dataSource).installPlugins()
}

object UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().header("User-Agent", "KRecruiter").build()
        return chain.proceed(request)
    }
}
