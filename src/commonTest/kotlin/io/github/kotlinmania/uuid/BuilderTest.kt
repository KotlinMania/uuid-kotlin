// port-lint: tests uuid/src/builder.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun testFromU128() {
        val high = 0xa1a2a3a4b1b2c1c2uL
        val low = 0xd1d2d3d4d5d6d7d8uL
        val uuid = Builder.fromU128(high, low).intoUuid()
        assertEquals("a1a2a3a4-b1b2-c1c2-d1d2-d3d4d5d6d7d8", uuid.hyphenated().toString())
    }

    @Test
    fun testFromU128Le() {
        val high = 0xd8d7d6d5d4d3d2d1uL
        val low = 0xc2c1b2b1a4a3a2a1uL
        val uuid = Builder.fromU128Le(high, low).intoUuid()
        assertEquals("a1a2a3a4-b1b2-c1c2-d1d2-d3d4d5d6d7d8", uuid.hyphenated().toString())
    }

    @Test
    fun testFromBytesRef() {
        val bytes = ByteArray(16) { it.toByte() }
        val uuid = Builder.fromBytesRef(bytes).intoUuid()
        assertEquals(Uuid.fromBytes(bytes), uuid)
    }

    @Test
    fun testFromSlice() {
        val bytes = ByteArray(16) { it.toByte() }
        val builderResult = Builder.fromSlice(bytes)
        assertTrue(builderResult.isSuccess)
        assertEquals(Uuid.fromBytes(bytes), builderResult.getOrThrow().intoUuid())
    }

    @Test
    fun testFromSliceLe() {
        val bytes = ByteArray(16) { it.toByte() }
        val builderResult = Builder.fromSliceLe(bytes)
        assertTrue(builderResult.isSuccess)
        assertEquals(Uuid.fromBytesLe(bytes), builderResult.getOrThrow().intoUuid())
    }

    @Test
    fun testFromFields() {
        val d1: UInt = 0xa1a2a3a4u
        val d2: UShort = 0xb1b2u
        val d3: UShort = 0xc1c2u
        val d4 = byteArrayOf(0xd1.toByte(), 0xd2.toByte(), 0xd3.toByte(), 0xd4.toByte(), 0xd5.toByte(), 0xd6.toByte(), 0xd7.toByte(), 0xd8.toByte())
        val uuid = Builder.fromFields(d1, d2, d3, d4).intoUuid()
        assertEquals("a1a2a3a4-b1b2-c1c2-d1d2-d3d4d5d6d7d8", uuid.hyphenated().toString())
    }

    @Test
    fun testFromFieldsLe() {
        val d1: UInt = 0xa4a3a2a1u
        val d2: UShort = 0xb2b1u
        val d3: UShort = 0xc2c1u
        val d4 = byteArrayOf(0xd1.toByte(), 0xd2.toByte(), 0xd3.toByte(), 0xd4.toByte(), 0xd5.toByte(), 0xd6.toByte(), 0xd7.toByte(), 0xd8.toByte())
        val uuid = Builder.fromFieldsLe(d1, d2, d3, d4).intoUuid()
        assertEquals("a1a2a3a4-b1b2-c1c2-d1d2-d3d4d5d6d7d8", uuid.hyphenated().toString())
    }

    @Test
    fun testFromMd5Bytes() {
        val bytes = ByteArray(16) { 0x55.toByte() }
        val uuid = Builder.fromMd5Bytes(bytes).intoUuid()
        assertEquals(Version.Md5, uuid.getVersion())
        assertEquals(Variant.RFC4122, uuid.getVariant())
    }

    @Test
    fun testFromSha1Bytes() {
        val bytes = ByteArray(16) { 0xaa.toByte() }
        val uuid = Builder.fromSha1Bytes(bytes).intoUuid()
        assertEquals(Version.Sha1, uuid.getVersion())
        assertEquals(Variant.RFC4122, uuid.getVariant())
    }

    @Test
    fun testFromUnixTimestampMillis() {
        val counterRandomBytes = ByteArray(10) { 0x12.toByte() }
        val uuid = Builder.fromUnixTimestampMillis(1_700_000_000_000uL, counterRandomBytes).intoUuid()
        assertEquals(Version.SortRand, uuid.getVersion())
        assertEquals(Variant.RFC4122, uuid.getVariant())
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
