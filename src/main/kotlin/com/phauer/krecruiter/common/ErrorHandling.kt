package com.phauer.krecruiter.common

/* The name "Result" conflicts with a class from the Kotlin std lib. */
sealed class Outcome<out T : Any> {
    data class Success<out T : Any>(val value: T) : Outcome<T>()
    data class Error(val message: String? = null, val cause: Exception? = null) : Outcome<Nothing>()
}

val <T> T.exhaustive: T
    get() = this