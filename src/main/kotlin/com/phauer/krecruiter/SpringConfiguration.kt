package com.phauer.krecruiter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.phauer.krecruiter.applicationApi.ApplicationDAO
import com.phauer.krecruiter.initializer.ApplicantInitializerDAO
import com.phauer.krecruiter.initializer.ApplicationInitializerDAO
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.util.concurrent.TimeUnit

@Configuration
class SpringConfiguration{

    @Bean
    fun objectMapper() = ObjectMapper()
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
    fun clock() = Clock.systemUTC()

    @Bean
    fun jdbi(): Jdbi {
        val ds = PGSimpleDataSource().apply {
            setUrl("jdbc:postgresql://localhost:6000/krecruiter")
            user = "user"
            password = "password"
            loadBalanceHosts = true
        }
        return Jdbi.create(ds).installPlugins()
    }

    @Bean
    fun applicationInitializerDAO(jdbi: Jdbi) = jdbi.onDemand(ApplicationInitializerDAO::class.java)

    @Bean
    fun applicantInitializerDAO(jdbi: Jdbi) = jdbi.onDemand(ApplicantInitializerDAO::class.java)

    @Bean
    fun applicationDao(jdbi: Jdbi) = jdbi.onDemand(ApplicationDAO::class.java)

}

object UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().header("User-Agent", "KRecruiter").build()
        return chain.proceed(request)
    }
}
