package com.phauer.krecruiter.kotlintest

import io.kotlintest.data.suspend.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.kotlintest.tables.row

class TableDrivenTest : FreeSpec() {
    init {
        "parse valid tokens" - {
            forall(
                row("1511443755_2", Token(1511443755, "2")),
                row("151175_13521", Token(151175, "13521")),
                row("151144375_id", Token(151144375, "id")),
                row("1511443759_1", Token(1511443759, "1")),
                row(null, null)
            ) { input: String?, expected: Token? ->
                "Input String: $input. Expected Token: $expected" {
                    parse(input).shouldBe(expected)
                }
            }
        }
        "parse valid tokens (data class)" - {
            forall(
                row(TestData(input = "1511443755_2", expected = Token(1511443755, "2"))),
                row(TestData(input = "151175_13521", expected = Token(151175, "13521"))),
                row(TestData(input = "151144375_id", expected = Token(151144375, "id"))),
                row(TestData(input = "1511443759_1", expected = Token(1511443759, "1"))),
                row(TestData(input = null, expected = null))
            ) { testData: TestData ->
                "Input String: ${testData.input}. Expected Token: ${testData.expected}" {
                    parse(testData.input).shouldBe(testData.expected)
                }
            }
        }
    }
}

data class TestData(
    val input: String?,
    val expected: Token?
)

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