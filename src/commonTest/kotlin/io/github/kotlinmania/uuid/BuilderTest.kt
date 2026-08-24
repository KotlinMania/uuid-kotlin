// port-lint: tests builder.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertEquals

class BuilderTest {
    @Test
    fun testFromRandomBytes() {
        val randomBytes =
            byteArrayOf(
                70,
                235.toByte(),
                208.toByte(),
                238.toByte(),
                14,
                109,
                67,
                201.toByte(),
                185.toByte(),
                13,
                204.toByte(),
                195.toByte(),
                90,
                145.toByte(),
                63,
                62,
            )
        val uuid = Builder.fromRandomBytes(randomBytes).intoUuid()

        assertEquals(Version.Random, uuid.getVersion())
        assertEquals(Variant.RFC4122, uuid.getVariant())
    }

    @Test
    fun testFromCustomBytes() {
        val customBytes =
            byteArrayOf(
                0xf,
                0xe,
                0xd,
                0xc,
                0xb,
                0xa,
                0x9,
                0x8,
                0x7,
                0x6,
                0x5,
                0x4,
                0x3,
                0x2,
                0x1,
                0x0,
            )
        val uuid = Builder.fromCustomBytes(customBytes).intoUuid()

        assertEquals(Version.Custom, uuid.getVersion())
        assertEquals(Variant.RFC4122, uuid.getVariant())
        assertEquals("0f0e0d0c-0b0a-8908-8706-050403020100", uuid.hyphenated().toString())
    }

    @Test
    fun testNil() {
        val uuid = Builder.nil().intoUuid()
        assertEquals("00000000-0000-0000-0000-000000000000", uuid.hyphenated().toString())
    }

    @Test
    fun testSetVariantAndVersion() {
        val bytes = ByteArray(16)
        val builder =
            Builder
                .fromBytes(bytes)
                .withVariant(Variant.RFC4122)
                .withVersion(Version.Random)

        val uuid = builder.intoUuid()
        assertEquals(Version.Random, uuid.getVersion())
        assertEquals(Variant.RFC4122, uuid.getVariant())
    }

    @Test
    fun testMax() {
        val uuid = Builder.max().intoUuid()
        assertEquals("ffffffff-ffff-ffff-ffff-ffffffffffff", uuid.hyphenated().toString())
        assertEquals(Version.Max, uuid.getVersion())
    }

    @Test
    fun testFromU64Pair() {
        val high = 0xa1a2a3a4b1b2c1c2uL
        val low = 0xd1d2d3d4d5d6d7d8uL
        val uuid = Builder.fromU64Pair(high, low).intoUuid()
        assertEquals("a1a2a3a4-b1b2-c1c2-d1d2-d3d4d5d6d7d8", uuid.hyphenated().toString())
    }

    @Test
    fun testFromRfc4122Timestamp() {
        val nodeId = byteArrayOf(1, 2, 3, 4, 5, 6)
        val uuid = Builder.fromRfc4122Timestamp(0x12345678uL, 0x9abcu, nodeId).intoUuid()
        assertEquals(Version.Mac, uuid.getVersion())
    }

    @Test
    fun testFromSortedRfc4122Timestamp() {
        val nodeId = byteArrayOf(1, 2, 3, 4, 5, 6)
        val uuid = Builder.fromSortedRfc4122Timestamp(0x12345678uL, 0x9abcu, nodeId).intoUuid()
        assertEquals(Version.SortMac, uuid.getVersion())
    }
}
