package com.phauer.krecruiter.kotest

import io.kotest.core.spec.style.FreeSpec
import io.kotest.engine.names.WithDataTestName
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe

class DataDrivenTest : FreeSpec() {
    init {
        "parse valid tokens" - {
            withTests(
                nameFn = { "Input String: ${it.first}. Expected Token: ${it.second}" },
                Pair("1511443755_2", Token(1511443755, "2")),
                Pair("151175_13521", Token(151175, "13521")),
                Pair("151144375_id", Token(151144375, "id")),
                Pair("1511443759_1", Token(1511443759, "1")),
                Pair(null, null)
            ) { (input, expected) ->
                parse(input).shouldBe(expected)
            }
        }
        "parse valid tokens (data class)" - {
            withTests(
                TestData(input = "1511443755_2", expected = Token(1511443755, "2")),
                TestData(input = "151175_13521", expected = Token(151175, "13521")),
                TestData(input = "151144375_id", expected = Token(151144375, "id")),
                TestData(input = "1511443759_1", expected = Token(1511443759, "1")),
                TestData(input = null, expected = null)
            ) { testData ->
                parse(testData.input).shouldBe(testData.expected)
            }
        }
    }
}

data class TestData(
    val input: String?,
    val expected: Token?
) : WithDataTestName {
    override fun dataTestName() = "Input String: $input. Expected Token: $expected"
}

fun parse(value: String?): Token? {
    value ?: return null
    val parts = value.split("_")
    if (parts.size != 2) {
        throw IllegalArgumentException(value, null)
    }
    try {
        val timestamp = java.lang.Long.parseUnsignedLong(parts[0])
        val id = parts[1]
        return Token(timestamp, id)
    } catch (ex: Exception) {
        throw IllegalArgumentException(value, ex)
    }
}

data class Token(
    val timestamp: Long,
    val id: String
)