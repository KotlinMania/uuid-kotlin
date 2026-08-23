package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UuidTest {
    @Test
    fun uuidDefaultValuesCompareByBytes() {
        val uuid1 = fixture()
        val uuid1Copy = fixture()
        val uuid2 =
            Uuid.fromBytes(
                byteArrayOf(
                    0xf9.toByte(),
                    0x16,
                    0x8c.toByte(),
                    0x5e,
                    0xce.toByte(),
                    0xb2.toByte(),
                    0x4f,
                    0xab.toByte(),
                    0xb6.toByte(),
                    0xbf.toByte(),
                    0x32,
                    0x9b.toByte(),
                    0xf3.toByte(),
                    0x9f.toByte(),
                    0xa1.toByte(),
                    0xe4.toByte(),
                ),
            )

        assertEquals(uuid1, uuid1Copy)
        assertNotEquals(uuid1, uuid2)
        assertTrue(uuid1 < uuid2)
    }

    @Test
    fun nilAndMaxExposeTheirVersionState() {
        val nil = Uuid.nil()
        val max = Uuid.max()
        val notNil = fixture()

        assertTrue(nil.isNil())
        assertFalse(notNil.isNil())
        assertEquals(Version.Nil, nil.getVersion())
        assertTrue(max.isMax())
        assertEquals(Version.Max, max.getVersion())
    }

    @Test
    fun nonConformingVersionIsUnknown() {
        val uuid =
            Uuid.fromBytes(byteArrayOf(4, 54, 67, 12, 43, 2, 2, 76, 32, 50, 87, 5, 1, 33, 43, 87))

        assertNull(uuid.getVersion())
    }

    @Test
    fun predefinedNamespacesMatchUpstreamBytes() {
        assertEquals("6ba7b810-9dad-11d1-80b4-00c04fd430c8", Uuid.NAMESPACE_DNS.toString())
        assertEquals("6ba7b811-9dad-11d1-80b4-00c04fd430c8", Uuid.NAMESPACE_URL.toString())
        assertEquals("6ba7b812-9dad-11d1-80b4-00c04fd430c8", Uuid.NAMESPACE_OID.toString())
        assertEquals("6ba7b814-9dad-11d1-80b4-00c04fd430c8", Uuid.NAMESPACE_X500.toString())
    }

    @Test
    fun fieldsRoundtrip() {
        val tail = byteArrayOf(0xd1.toByte(), 0xd2.toByte(), 0xd3.toByte(), 0xd4.toByte(), 0xd5.toByte(), 0xd6.toByte(), 0xd7.toByte(), 0xd8.toByte())
        val uuid = Uuid.fromFields(0xa1a2a3a4u, 0xb1b2u, 0xc1c2u, tail)
        val fields = uuid.asFields()

        assertEquals(0xa1a2a3a4u, fields.d1)
        assertEquals(0xb1b2u, fields.d2)
        assertEquals(0xc1c2u, fields.d3)
        assertContentEquals(tail, fields.d4)
        assertEquals("a1a2a3a4b1b2c1c2d1d2d3d4d5d6d7d8", uuid.simple().toString())
    }

    @Test
    fun littleEndianFieldsRoundtrip() {
        val tail = byteArrayOf(0xd1.toByte(), 0xd2.toByte(), 0xd3.toByte(), 0xd4.toByte(), 0xd5.toByte(), 0xd6.toByte(), 0xd7.toByte(), 0xd8.toByte())
        val uuid = Uuid.fromFieldsLe(0xa4a3a2a1u, 0xb2b1u, 0xc2c1u, tail)

        assertEquals("a1a2a3a4b1b2c1c2d1d2d3d4d5d6d7d8", uuid.simple().toString())
        assertEquals(UuidFields(0xa4a3a2a1u, 0xb2b1u, 0xc2c1u, tail), uuid.toFieldsLe())
    }

    @Test
    fun byteConversionsRoundtrip() {
        val bytes =
            byteArrayOf(
                0xa1.toByte(),
                0xa2.toByte(),
                0xa3.toByte(),
                0xa4.toByte(),
                0xb1.toByte(),
                0xb2.toByte(),
                0xc1.toByte(),
                0xc2.toByte(),
                0xd1.toByte(),
                0xd2.toByte(),
                0xd3.toByte(),
                0xd4.toByte(),
                0xd5.toByte(),
                0xd6.toByte(),
                0xd7.toByte(),
                0xd8.toByte(),
            )
        val uuid = Uuid.fromBytes(bytes)
        val littleEndian = uuid.toBytesLe()

        assertContentEquals(bytes, uuid.asBytes())
        assertEquals(uuid, Uuid.fromBytesLe(littleEndian))
        assertEquals(0xa1a2a3a4b1b2c1c2uL to 0xd1d2d3d4d5d6d7d8uL, uuid.asU64Pair())
    }

    @Test
    fun variantDetectionMatchesReservedPatterns() {
        assertEquals(Variant.RFC4122, fixture().getVariant())
        assertEquals(Variant.Microsoft, Uuid.parseStr("936DA01F9ABD4d9dC0C702AF85C822A8").getVariant())
        assertEquals(Variant.NCS, Uuid.parseStr("f81d4fae-7dec-11d0-7765-00a0c91e6bf6").getVariant())
    }

    @Test
    fun fromU64PairRoundtrip() {
        val high = 0xa1a2a3a4b1b2c1c2uL
        val low = 0xd1d2d3d4d5d6d7d8uL
        val uuid = Uuid.fromU64Pair(high, low)
        assertEquals(high to low, uuid.asU64Pair())
    }

    @Test
    fun fromSliceValidation() {
        val valid = ByteArray(16) { it.toByte() }
        val invalid = ByteArray(15) { it.toByte() }
        assertTrue(Uuid.fromSlice(valid).isSuccess)
        assertTrue(Uuid.fromSlice(invalid).isFailure)
    }

    @Test
    fun tryParseAndTryParseAscii() {
        val str = "6ba7b810-9dad-11d1-80b4-00c04fd430c8"
        val parsed = Uuid.tryParse(str).getOrThrow()
        assertEquals(Uuid.NAMESPACE_DNS, parsed)
        val parsedAscii = Uuid.tryParseAscii(str.encodeToByteArray()).getOrThrow()
        assertEquals(Uuid.NAMESPACE_DNS, parsedAscii)
        assertTrue(Uuid.tryParse("invalid-uuid").isFailure)
    }

    @Test
    fun encodeBufferHasValidLength() {
        val buf = Uuid.encodeBuffer()
        assertEquals(Urn.LENGTH, buf.size)
    }

    @Test
    fun intoBytesCopiesUnderlyingBytes() {
        val uuid = fixture()
        assertContentEquals(uuid.asBytes(), uuid.intoBytes())
    }
}

internal fun fixture(): Uuid =
    Uuid.fromBytes(
        byteArrayOf(
            0xf9.toByte(),
            0x16,
            0x8c.toByte(),
            0x5e,
            0xce.toByte(),
            0xb2.toByte(),
            0x4f,
            0xaa.toByte(),
            0xb6.toByte(),
            0xbf.toByte(),
            0x32,
            0x9b.toByte(),
            0xf3.toByte(),
            0x9f.toByte(),
            0xa1.toByte(),
            0xe4.toByte(),
        ),
    )
