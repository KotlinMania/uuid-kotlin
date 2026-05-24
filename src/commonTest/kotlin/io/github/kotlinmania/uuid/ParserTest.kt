package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ParserTest {
    @Test
    fun parsesAllCanonicalInputShapes() {
        val fromHyphenated = Uuid.parseStr("67e55044-10b1-426f-9247-bb680e5fe0c8")
        val fromSimple = Uuid.parseStr("67e5504410b1426f9247bb680e5fe0c8")
        val fromUrn = Uuid.parseStr("urn:uuid:67e55044-10b1-426f-9247-bb680e5fe0c8")
        val fromGuid = Uuid.parseStr("{67e55044-10b1-426f-9247-bb680e5fe0c8}")

        assertEquals(fromHyphenated, fromSimple)
        assertEquals(fromHyphenated, fromUrn)
        assertEquals(fromHyphenated, fromGuid)
        assertEquals(Version.Random, fromHyphenated.getVersion())
        assertEquals(Variant.RFC4122, fromHyphenated.getVariant())
    }

    @Test
    fun parseStrPreservesDetailedDiagnostics() {
        assertParseError("", "invalid length: expected length 32 for simple format, found 0")
        assertParseError("!", "invalid character: expected an optional prefix of `urn:uuid:` followed by [0-9a-fA-F-], found `!` at 1")
        assertParseError("F9168C5E-CEB2-4faa-B6BF-329BF39FA1E45", "invalid group length in group 4: expected 12, found 13")
        assertParseError("F9168C5E-CEB2-4faa-BGBF-329BF39FA1E4", "invalid character: expected an optional prefix of `urn:uuid:` followed by [0-9a-fA-F-], found `G` at 21")
        assertParseError("F9168C5E-CEB2F4faaFB6BFF329BF39FA1E4", "invalid group count: expected 5, found 2")
        assertParseError("{00000000000000000000000000000000}", "invalid group count: expected 5, found 1")
    }

    @Test
    fun roundtripsThroughEveryFormatter() {
        val uuid = fixture()

        assertEquals(uuid, Uuid.parseStr(uuid.toString()))
        assertEquals(uuid, Uuid.parseStr(uuid.simple().toString()))
        assertEquals(uuid, Uuid.parseStr(uuid.urn().toString()))
        assertEquals(uuid, Uuid.parseStr(uuid.braced().toString()))
    }

    @Test
    fun tryParseAsciiReturnsGenericErrors() {
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
