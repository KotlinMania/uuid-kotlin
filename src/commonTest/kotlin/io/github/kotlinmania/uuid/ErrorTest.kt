// port-lint: tests error.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorTest {
    @Test
    fun invalidUuidReportsInvalidUtf8() {
        val error = InvalidUuid(byteArrayOf(0xff.toByte())).intoErr()

        assertEquals("non-UTF8 input", error.toString())
    }

    @Test
    fun invalidUuidReportsNonHexCharacter() {
        val error = InvalidUuid("67e55044-10b1-426f-9247-bb680e5fe0cz".encodeToByteArray()).intoErr()

        assertEquals(
            "invalid character: expected an optional prefix of `urn:uuid:` followed by [0-9a-fA-F-], found `z` at 36",
            error.toString(),
        )
    }

    @Test
    fun invalidUuidReportsSimpleLength() {
        val error = InvalidUuid("67e5504410b1426f9247bb680e5fe0c".encodeToByteArray()).intoErr()

        assertEquals("invalid length: expected length 32 for simple format, found 31", error.toString())
    }

    @Test
    fun invalidUuidReportsGroupCount() {
        val error = InvalidUuid("67e55044-10b1-426f-9247bb680e5fe0c8".encodeToByteArray()).intoErr()

        assertEquals("invalid group count: expected 5, found 4", error.toString())
    }

    @Test
    fun invalidUuidReportsGroupLength() {
        val error = InvalidUuid("67e550441-10b1-426f-9247-bb680e5fe0c8".encodeToByteArray()).intoErr()

        assertEquals("invalid group length in group 0: expected 8, found 9", error.toString())
    }

    @Test
    fun invalidUuidAccountsForAcceptedPrefixes() {
        val braced = InvalidUuid("{67e550441-10b1-426f-9247-bb680e5fe0c8}".encodeToByteArray()).intoErr()
        val urn = InvalidUuid("urn:uuid:67e550441-10b1-426f-9247-bb680e5fe0c8".encodeToByteArray()).intoErr()

        assertEquals("invalid group length in group 0: expected 8, found 9", braced.toString())
        assertEquals("invalid group length in group 0: expected 8, found 9", urn.toString())
    }
}
