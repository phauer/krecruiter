package com.phauer.krecruiter.common

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionControllerAdvice {

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun exception(ex: HttpMessageNotReadableException) = when (val cause = ex.cause) {
        is MissingKotlinParameterException -> ResponseEntity.badRequest().body("""{"errorMessage":"${cause.msg}"}""")
        else -> ResponseEntity.badRequest().body(ex.message)
    }

}