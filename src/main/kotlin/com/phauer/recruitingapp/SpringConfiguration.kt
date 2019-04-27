package com.phauer.recruitingapp

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class SpringConfiguration{

      // TODO show config with apply {}
    @Bean
    fun restTemplate() = RestTemplate()
}