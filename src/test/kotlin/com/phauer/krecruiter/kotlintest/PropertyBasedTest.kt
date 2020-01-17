package com.phauer.krecruiter.kotlintest

import io.kotlintest.matchers.string.shouldHaveLength
import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.FreeSpec

class PropertyBasedTest : FreeSpec() {
    init {
        "String size" {
            assertAll(500, Gen.string(maxSize = 10), Gen.string(maxSize = 10)) { a: String, b: String ->
                (a + b).shouldHaveLength(a.length + b.length)
            }
        }
    }
}