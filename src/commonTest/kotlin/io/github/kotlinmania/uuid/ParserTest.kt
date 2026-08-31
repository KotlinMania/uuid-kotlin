// port-lint: tests uuid/src/parser.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ParserTest {
    @Test
    fun testParseUuidV4Valid() {
        val fromHyphenated = Uuid.parseStr("67e55044-10b1-426f-9247-bb680e5fe0c8")
        val fromSimple = Uuid.parseStr("67e5504410b1426f9247bb680e5fe0c8")
        val fromUrn = Uuid.parseStr("urn:uuid:67e55044-10b1-426f-9247-bb680e5fe0c8")
        val fromGuid = Uuid.parseStr("{67e55044-10b1-426f-9247-bb680e5fe0c8}")

        assertEquals(fromHyphenated, fromSimple)
        assertEquals(fromHyphenated, fromUrn)
        assertEquals(fromHyphenated, fromGuid)

        assertTrue(runCatching { Uuid.parseStr("00000000000000000000000000000000") }.isSuccess)
        assertTrue(runCatching { Uuid.parseStr("67e55044-10b1-426f-9247-bb680e5fe0c8") }.isSuccess)
        assertTrue(runCatching { Uuid.parseStr("F9168C5E-CEB2-4faa-B6BF-329BF39FA1E4") }.isSuccess)
        assertTrue(runCatching { Uuid.parseStr("67e5504410b1426f9247bb680e5fe0c8") }.isSuccess)
        assertTrue(runCatching { Uuid.parseStr("01020304-1112-2122-3132-414243444546") }.isSuccess)
        assertTrue(runCatching { Uuid.parseStr("urn:uuid:67e55044-10b1-426f-9247-bb680e5fe0c8") }.isSuccess)
        assertTrue(runCatching { Uuid.parseStr("{6d93bade-bd9f-4e13-8914-9474e1e3567b}") }.isSuccess)

        // Nil
        val nil = Uuid.nil()
        assertEquals(Uuid.parseStr("00000000000000000000000000000000"), nil)
        assertEquals(Uuid.parseStr("00000000-0000-0000-0000-000000000000"), nil)
    }

    @Test
    fun testParseUuidV4Invalid() {
        assertParseError("", "invalid length: expected length 32 for simple format, found 0")
        assertParseError("!", "invalid character: expected an optional prefix of `urn:uuid:` followed by [0-9a-fA-F-], found `!` at 1")
        assertParseError("F9168C5E-CEB2-4faa-B6BF-329BF39FA1E45", "invalid group length in group 4: expected 12, found 13")
        assertParseError("F9168C5E-CEB2-4faa-BBF-329BF39FA1E4", "invalid group length in group 3: expected 4, found 3")
        assertParseError("F9168C5E-CEB2-4faa-BGBF-329BF39FA1E4", "invalid character: expected an optional prefix of `urn:uuid:` followed by [0-9a-fA-F-], found `G` at 21")
        assertParseError("F9168C5E-CEB2F4faaFB6BFF329BF39FA1E4", "invalid group count: expected 5, found 2")
        assertParseError("F9168C5E-CEB2-4faaFB6BFF329BF39FA1E4", "invalid group count: expected 5, found 3")
        assertParseError("F9168C5E-CEB2-4faa-B6BFF329BF39FA1E4", "invalid group count: expected 5, found 4")
        assertParseError("F9168C5E-CEB2-4faa", "invalid group count: expected 5, found 3")
        assertParseError("F9168C5E-CEB2-4faaXB6BFF329BF39FA1E4", "invalid character: expected an optional prefix of `urn:uuid:` followed by [0-9a-fA-F-], found `X` at 19")
        assertParseError("{F9168C5E-CEB2-4faa9B6BFF329BF39FA1E41", "invalid character: expected an optional prefix of `urn:uuid:` followed by [0-9a-fA-F-], found `{` at 1")
        assertParseError("{F9168C5E-CEB2-4faa9B6BFF329BF39FA1E41}", "invalid group count: expected 5, found 3")
        assertParseError("F9168C5E-CEB-24fa-eB6BFF32-BF39FA1E4", "invalid group length in group 1: expected 4, found 3")
        assertParseError("01020304-1112-2122-3132-41424344", "invalid group length in group 4: expected 12, found 8")
        assertParseError("67e5504410b1426f9247bb680e5fe0c", "invalid length: expected length 32 for simple format, found 31")
        assertParseError("67e5504410b1426f9247bb680e5fe0c88", "invalid length: expected length 32 for simple format, found 33")
        assertParseError("67e5504410b1426f9247bb680e5fe0cg8", "invalid character: expected an optional prefix of `urn:uuid:` followed by [0-9a-fA-F-], found `g` at 32")
        assertParseError("67e5504410b1426%9247bb680e5fe0c8", "invalid character: expected an optional prefix of `urn:uuid:` followed by [0-9a-fA-F-], found `%` at 16")
        assertParseError("231231212212423424324323477343246663", "invalid length: expected length 32 for simple format, found 36")
        assertParseError("{00000000000000000000000000000000}", "invalid group count: expected 5, found 1")
        assertParseError("67e550X410b1426f9247bb680e5fe0cd", "invalid character: expected an optional prefix of `urn:uuid:` followed by [0-9a-fA-F-], found `X` at 7")
        assertParseError("67e550-4105b1426f9247bb680e5fe0c", "invalid group count: expected 5, found 2")
        assertParseError("F9168C5E-CEB2-4faa-B6BF1-02BF39FA1E4", "invalid group length in group 3: expected 4, found 5")
        val unicodeError = assertFailsWith<Error> { Uuid.parseStr("\uDAB3\uDF3C") }
        assertTrue(unicodeError.toString().startsWith("invalid character: expected an optional prefix of `urn:uuid:` followed by [0-9a-fA-F-]"))
    }

    @Test
    fun testRoundtripDefault() {
        val uuidOrig = new()
        val origStr = uuidOrig.toString()
        val uuidOut = Uuid.parseStr(origStr)
        assertEquals(uuidOrig, uuidOut)
    }

    @Test
    fun testRoundtripHyphenated() {
        val uuidOrig = new()
        val origStr = uuidOrig.hyphenated().toString()
        val uuidOut = Uuid.parseStr(origStr)
        assertEquals(uuidOrig, uuidOut)
    }

    @Test
    fun testRoundtripSimple() {
        val uuidOrig = new()
        val origStr = uuidOrig.simple().toString()
        val uuidOut = Uuid.parseStr(origStr)
        assertEquals(uuidOrig, uuidOut)
    }

    @Test
    fun testRoundtripUrn() {
        val uuidOrig = new()
        val origStr = uuidOrig.urn().toString()
        val uuidOut = Uuid.parseStr(origStr)
        assertEquals(uuidOrig, uuidOut)
    }

    @Test
    fun testRoundtripBraced() {
        val uuidOrig = new()
        val origStr = uuidOrig.braced().toString()
        val uuidOut = Uuid.parseStr(origStr)
        assertEquals(uuidOrig, uuidOut)
    }

    @Test
    fun testRoundtripParseUrn() {
        val uuidOrig = new()
        val origStr = uuidOrig.urn().toString()
        val uuidOut = Uuid.fromBytes(parseUrn(origStr.encodeToByteArray()).getOrThrow())
        assertEquals(uuidOrig, uuidOut)
    }

    @Test
    fun testRoundtripParseBraced() {
        val uuidOrig = new()
        val origStr = uuidOrig.braced().toString()
        val uuidOut = Uuid.fromBytes(parseBraced(origStr.encodeToByteArray()).getOrThrow())
        assertEquals(uuidOrig, uuidOut)
    }

    @Test
    fun testTryParseAsciiNonUtf8() {
        assertTrue(Uuid.tryParseAscii("67e55044-10b1-426f-9247-bb680e5\u0000e0c8".encodeToByteArray()).isFailure)
    }

    private fun assertParseError(
        input: String,
        message: String,
    ) {
        val error = assertFailsWith<Error> { Uuid.parseStr(input) }

        assertEquals(message, error.toString())
    }
}
