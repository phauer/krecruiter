package com.phauer.krecruiter.common


import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun <T : Any> T.logger(): Lazy<Logger> {
    return lazyOf(LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass).name))
}

private fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return if (ofClass.kotlin.isCompanion && ofClass.enclosingClass != null) {
        ofClass.enclosingClass
    } else {
        ofClass
    }
}