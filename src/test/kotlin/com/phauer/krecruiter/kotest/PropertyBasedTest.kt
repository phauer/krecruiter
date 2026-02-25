package com.phauer.krecruiter.kotest

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class PropertyBasedTest : FreeSpec() {
    init {
        "String size" {
            checkAll(iterations = 500, Arb.string(maxSize = 10), Arb.string(maxSize = 10)) { a: String, b: String ->
                (a + b).shouldHaveLength(a.length + b.length)
            }
        }
    }
}