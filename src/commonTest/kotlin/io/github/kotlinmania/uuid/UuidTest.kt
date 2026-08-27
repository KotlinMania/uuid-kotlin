// port-lint: tests lib.rs
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
    fun testUuidCompare() {
        val uuid1 = new()
        val uuid2 = new2()

        assertEquals(uuid1, uuid1)
        assertEquals(uuid2, uuid2)

        assertNotEquals(uuid1, uuid2)
        assertNotEquals(uuid2, uuid1)
    }

    @Test
    fun testUuidDefault() {
        val defaultUuid = Uuid.nil()
        val nilUuid = Uuid.nil()

        assertEquals(defaultUuid, nilUuid)
    }

    @Test
    fun testUuidDisplay() {
        val uuid = new()
        val s = uuid.toString()

        assertEquals(s, uuid.hyphenated().toString())
        assertEquals(36, s.length)
        assertTrue(s.all { it.isLowerCase() || it.isDigit() || it == '-' })
    }

    @Test
    fun testUuidLowerhex() {
        val uuid = new()
        val s = uuid.hyphenated().encodeLower(Uuid.encodeBuffer())
        assertEquals(36, s.length)
        assertTrue(s.all { it.isLowerCase() || it.isDigit() || it == '-' })
    }

    @Test
    fun testUuidOperatorEq() {
        val uuid1 = new()
        val uuid1Dup = uuid1
        val uuid2 = new2()

        assertTrue(uuid1 == uuid1)
        assertTrue(uuid1 == uuid1Dup)
        assertTrue(uuid1Dup == uuid1)

        assertTrue(uuid1 != uuid2)
        assertTrue(uuid2 != uuid1)
        assertTrue(uuid1Dup != uuid2)
        assertTrue(uuid2 != uuid1Dup)
    }

    @Test
    fun testUuidToString() {
        val uuid = new()
        val s = uuid.toString()

        assertEquals(36, s.length)
        assertTrue(s.all { it.isLowerCase() || it.isDigit() || it == '-' })
    }

    @Test
    fun testNonConforming() {
        val fromBytes =
            Uuid.fromBytes(byteArrayOf(4, 54, 67, 12, 43, 2, 2, 76, 32, 50, 87, 5, 1, 33, 43, 87))

        assertNull(fromBytes.getVersion())
    }

    @Test
    fun testNil() {
        val nil = Uuid.nil()
        val notNil = new()

        assertTrue(nil.isNil())
        assertFalse(notNil.isNil())

        assertEquals(Version.Nil, nil.getVersion())
        assertEquals(Version.Random, notNil.getVersion())

        assertEquals(
            nil,
            Builder.fromBytes(ByteArray(16))
                .withVersion(Version.Nil)
                .intoUuid(),
        )
    }

    @Test
    fun testMax() {
        val max = Uuid.max()
        val notMax = new()

        assertTrue(max.isMax())
        assertFalse(notMax.isMax())

        assertEquals(Version.Max, max.getVersion())
        assertEquals(Version.Random, notMax.getVersion())

        assertEquals(
            max,
            Builder.fromBytes(ByteArray(16) { 0xff.toByte() })
                .withVersion(Version.Max)
                .intoUuid(),
        )
    }

    @Test
    fun testPredefinedNamespaces() {
        assertEquals(
            "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
            Uuid.NAMESPACE_DNS.hyphenated().toString(),
        )
        assertEquals(
            "6ba7b811-9dad-11d1-80b4-00c04fd430c8",
            Uuid.NAMESPACE_URL.hyphenated().toString(),
        )
        assertEquals(
            "6ba7b812-9dad-11d1-80b4-00c04fd430c8",
            Uuid.NAMESPACE_OID.hyphenated().toString(),
        )
        assertEquals(
            "6ba7b814-9dad-11d1-80b4-00c04fd430c8",
            Uuid.NAMESPACE_X500.hyphenated().toString(),
        )
    }

    @Test
    fun testGetVersionV3() {
        val uuid = Uuid.newV3(Uuid.NAMESPACE_DNS, "rust-lang.org".encodeToByteArray())

        assertEquals(Version.Md5, uuid.getVersion())
        assertEquals(3, uuid.getVersionNum())
    }

    @Test
    fun testGetTimestampUnsupportedVersion() {
        val uuid = new()

        assertNotEquals(Version.Mac, uuid.getVersion())
        assertNotEquals(Version.SortMac, uuid.getVersion())
        assertNotEquals(Version.SortRand, uuid.getVersion())

        assertNull(uuid.getTimestamp())
    }

    @Test
    fun testGetNodeIdUnsupportedVersion() {
        val uuid = new()

        assertNotEquals(Version.Mac, uuid.getVersion())
        assertNotEquals(Version.SortMac, uuid.getVersion())

        assertNull(uuid.getNodeId())
    }

    @Test
    fun testGetVariant() {
        val uuid1 = new()
        val uuid2 = Uuid.parseStr("550e8400-e29b-41d4-a716-446655440000")
        val uuid3 = Uuid.parseStr("67e55044-10b1-426f-9247-bb680e5fe0c8")
        val uuid4 = Uuid.parseStr("936DA01F9ABD4d9dC0C702AF85C822A8")
        val uuid5 = Uuid.parseStr("F9168C5E-CEB2-4faa-D6BF-329BF39FA1E4")
        val uuid6 = Uuid.parseStr("f81d4fae-7dec-11d0-7765-00a0c91e6bf6")

        assertEquals(Variant.RFC4122, uuid1.getVariant())
        assertEquals(Variant.RFC4122, uuid2.getVariant())
        assertEquals(Variant.RFC4122, uuid3.getVariant())
        assertEquals(Variant.Microsoft, uuid4.getVariant())
        assertEquals(Variant.Microsoft, uuid5.getVariant())
        assertEquals(Variant.NCS, uuid6.getVariant())
    }

    @Test
    fun testToSimpleString() {
        val uuid1 = new()
        val s = uuid1.simple().toString()

        assertEquals(32, s.length)
        assertTrue(s.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' })
    }

    @Test
    fun testHyphenatedString() {
        val uuid1 = new()
        val s = uuid1.hyphenated().toString()

        assertEquals(36, s.length)
        assertTrue(s.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' || it == '-' })
    }

    @Test
    fun testUpperLowerHex() {
        val u = new()
        val lowerHyphen = u.hyphenated().encodeLower(Uuid.encodeBuffer())
        val upperHyphen = u.hyphenated().encodeUpper(Uuid.encodeBuffer())
        val lowerSimple = u.simple().encodeLower(Uuid.encodeBuffer())
        val upperSimple = u.simple().encodeUpper(Uuid.encodeBuffer())

        assertEquals(36, lowerHyphen.length)
        assertTrue(lowerHyphen.all { it.isLowerCase() || it.isDigit() || it == '-' })
        assertEquals(36, upperHyphen.length)
        assertTrue(upperHyphen.all { it.isUpperCase() || it.isDigit() || it == '-' })

        assertEquals(32, lowerSimple.length)
        assertTrue(lowerSimple.all { it.isLowerCase() || it.isDigit() })
        assertEquals(32, upperSimple.length)
        assertTrue(upperSimple.all { it.isUpperCase() || it.isDigit() })
    }

    @Test
    fun testToUrnString() {
        val uuid1 = new()
        val ss = uuid1.urn().toString()
        val s = ss.substring(9)

        assertTrue(ss.startsWith("urn:uuid:"))
        assertEquals(36, s.length)
        assertTrue(s.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' || it == '-' })
    }

    @Test
    fun testToSimpleStringMatching() {
        val uuid1 = new()

        val hs = uuid1.hyphenated().toString()
        val ss = uuid1.simple().toString()

        val hsn = hs.filter { it != '-' }

        assertEquals(hsn, ss)
    }

    @Test
    fun testStringRoundtrip() {
        val uuid = new()

        val hs = uuid.hyphenated().toString()
        val uuidHs = Uuid.parseStr(hs)
        assertEquals(uuidHs, uuid)

        val ss = uuid.toString()
        val uuidSs = Uuid.parseStr(ss)
        assertEquals(uuidSs, uuid)
    }

    @Test
    fun testFromFields() {
        val d1: UInt = 0xa1a2a3a4u
        val d2: UShort = 0xb1b2u
        val d3: UShort = 0xc1c2u
        val d4 = byteArrayOf(0xd1.toByte(), 0xd2.toByte(), 0xd3.toByte(), 0xd4.toByte(), 0xd5.toByte(), 0xd6.toByte(), 0xd7.toByte(), 0xd8.toByte())

        val u = Uuid.fromFields(d1, d2, d3, d4)

        val expected = "a1a2a3a4b1b2c1c2d1d2d3d4d5d6d7d8"
        val result = u.simple().toString()
        assertEquals(expected, result)
    }

    @Test
    fun testFromFieldsLe() {
        val d1: UInt = 0xa4a3a2a1u
        val d2: UShort = 0xb2b1u
        val d3: UShort = 0xc2c1u
        val d4 = byteArrayOf(0xd1.toByte(), 0xd2.toByte(), 0xd3.toByte(), 0xd4.toByte(), 0xd5.toByte(), 0xd6.toByte(), 0xd7.toByte(), 0xd8.toByte())

        val u = Uuid.fromFieldsLe(d1, d2, d3, d4)

        val expected = "a1a2a3a4b1b2c1c2d1d2d3d4d5d6d7d8"
        val result = u.simple().toString()
        assertEquals(expected, result)
    }

    @Test
    fun testAsFields() {
        val u = new()
        val (d1, d2, d3, d4) = u.asFields()

        assertNotEquals(0u, d1)
        assertNotEquals(0u.toUShort(), d2)
        assertNotEquals(0u.toUShort(), d3)
        assertEquals(8, d4.size)
        assertFalse(d4.all { it == 0.toByte() })
    }

    @Test
    fun testFieldsRoundtrip() {
        val d1In: UInt = 0xa1a2a3a4u
        val d2In: UShort = 0xb1b2u
        val d3In: UShort = 0xc1c2u
        val d4In = byteArrayOf(0xd1.toByte(), 0xd2.toByte(), 0xd3.toByte(), 0xd4.toByte(), 0xd5.toByte(), 0xd6.toByte(), 0xd7.toByte(), 0xd8.toByte())

        val u = Uuid.fromFields(d1In, d2In, d3In, d4In)
        val (d1Out, d2Out, d3Out, d4Out) = u.asFields()

        assertEquals(d1In, d1Out)
        assertEquals(d2In, d2Out)
        assertEquals(d3In, d3Out)
        assertContentEquals(d4In, d4Out)
    }

    @Test
    fun testFieldsLeRoundtrip() {
        val d1In: UInt = 0xa4a3a2a1u
        val d2In: UShort = 0xb2b1u
        val d3In: UShort = 0xc2c1u
        val d4In = byteArrayOf(0xd1.toByte(), 0xd2.toByte(), 0xd3.toByte(), 0xd4.toByte(), 0xd5.toByte(), 0xd6.toByte(), 0xd7.toByte(), 0xd8.toByte())

        val u = Uuid.fromFieldsLe(d1In, d2In, d3In, d4In)
        val (d1Out, d2Out, d3Out, d4Out) = u.toFieldsLe()

        assertEquals(d1In, d1Out)
        assertEquals(d2In, d2Out)
        assertEquals(d3In, d3Out)
        assertContentEquals(d4In, d4Out)
    }

    @Test
    fun testFieldsLeAreActuallyLe() {
        val d1In: UInt = 0xa1a2a3a4u
        val d2In: UShort = 0xb1b2u
        val d3In: UShort = 0xc1c2u
        val d4In = byteArrayOf(0xd1.toByte(), 0xd2.toByte(), 0xd3.toByte(), 0xd4.toByte(), 0xd5.toByte(), 0xd6.toByte(), 0xd7.toByte(), 0xd8.toByte())

        val u = Uuid.fromFields(d1In, d2In, d3In, d4In)
        val (d1Out, d2Out, d3Out, d4Out) = u.toFieldsLe()

        assertEquals(d1In, d1Out.reverseBytes())
        assertEquals(d2In, d2Out.reverseBytes())
        assertEquals(d3In, d3Out.reverseBytes())
        assertContentEquals(d4In, d4Out)
    }

    @Test
    fun testFromU128() {
        val highIn: ULong = 0xa1a2a3a4b1b2c1c2uL
        val lowIn: ULong = 0xd1d2d3d4d5d6d7d8uL

        val u = Uuid.fromU128(highIn, lowIn)

        val expected = "a1a2a3a4b1b2c1c2d1d2d3d4d5d6d7d8"
        val result = u.simple().toString()
        assertEquals(expected, result)
    }

    @Test
    fun testFromU128Le() {
        val highIn: ULong = 0xd8d7d6d5d4d3d2d1uL
        val lowIn: ULong = 0xc2c1b2b1a4a3a2a1uL

        val u = Uuid.fromU128Le(highIn, lowIn)

        val expected = "a1a2a3a4b1b2c1c2d1d2d3d4d5d6d7d8"
        val result = u.simple().toString()
        assertEquals(expected, result)
    }

    @Test
    fun testFromU64Pair() {
        val highIn: ULong = 0xa1a2a3a4b1b2c1c2uL
        val lowIn: ULong = 0xd1d2d3d4d5d6d7d8uL

        val u = Uuid.fromU64Pair(highIn, lowIn)

        val expected = "a1a2a3a4b1b2c1c2d1d2d3d4d5d6d7d8"
        val result = u.simple().toString()
        assertEquals(expected, result)
    }

    @Test
    fun testU128Roundtrip() {
        val highIn: ULong = 0xa1a2a3a4b1b2c1c2uL
        val lowIn: ULong = 0xd1d2d3d4d5d6d7d8uL

        val u = Uuid.fromU128(highIn, lowIn)
        val (highOut, lowOut) = u.asU128()

        assertEquals(highIn, highOut)
        assertEquals(lowIn, lowOut)
    }

    @Test
    fun testU128LeRoundtrip() {
        val highIn: ULong = 0xd8d7d6d5d4d3d2d1uL
        val lowIn: ULong = 0xc2c1b2b1a4a3a2a1uL

        val u = Uuid.fromU128Le(highIn, lowIn)
        val (highOut, lowOut) = u.toU128Le()

        assertEquals(highIn, highOut)
        assertEquals(lowIn, lowOut)
    }

    @Test
    fun testU128LeIsActuallyLe() {
        val vInHigh: ULong = 0xa1a2a3a4b1b2c1c2uL
        val vInLow: ULong = 0xd1d2d3d4d5d6d7d8uL

        val u = Uuid.fromU128(vInHigh, vInLow)
        val (vOutHigh, vOutLow) = u.toU128Le()

        assertEquals(vInHigh, vOutLow.reverseBytes())
        assertEquals(vInLow, vOutHigh.reverseBytes())
    }

    @Test
    fun testU64PairRoundtrip() {
        val highIn: ULong = 0xa1a2a3a4b1b2c1c2uL
        val lowIn: ULong = 0xd1d2d3d4d5d6d7d8uL

        val u = Uuid.fromU64Pair(highIn, lowIn)
        val (highOut, lowOut) = u.asU64Pair()

        assertEquals(highIn, highOut)
        assertEquals(lowIn, lowOut)
    }

    @Test
    fun testFromSlice() {
        val b =
            byteArrayOf(
                0xa1.toByte(), 0xa2.toByte(), 0xa3.toByte(), 0xa4.toByte(),
                0xb1.toByte(), 0xb2.toByte(), 0xc1.toByte(), 0xc2.toByte(),
                0xd1.toByte(), 0xd2.toByte(), 0xd3.toByte(), 0xd4.toByte(),
                0xd5.toByte(), 0xd6.toByte(), 0xd7.toByte(), 0xd8.toByte(),
            )

        val u = Uuid.fromSlice(b).getOrThrow()
        val expected = "a1a2a3a4b1b2c1c2d1d2d3d4d5d6d7d8"

        assertEquals(expected, u.simple().toString())
    }

    @Test
    fun testFromBytes() {
        val b =
            byteArrayOf(
                0xa1.toByte(), 0xa2.toByte(), 0xa3.toByte(), 0xa4.toByte(),
                0xb1.toByte(), 0xb2.toByte(), 0xc1.toByte(), 0xc2.toByte(),
                0xd1.toByte(), 0xd2.toByte(), 0xd3.toByte(), 0xd4.toByte(),
                0xd5.toByte(), 0xd6.toByte(), 0xd7.toByte(), 0xd8.toByte(),
            )

        val u = Uuid.fromBytes(b)
        val expected = "a1a2a3a4b1b2c1c2d1d2d3d4d5d6d7d8"

        assertEquals(expected, u.simple().toString())
    }

    @Test
    fun testAsBytes() {
        val u = new()
        val ub = u.asBytes()

        assertEquals(16, ub.size)
        assertFalse(ub.all { it == 0.toByte() })
    }

    @Test
    fun testConvertVec() {
        val u = new()
        val ub = u.asBytes()
        val list = u.asBytes().toList()
        assertEquals(ub.toList(), list)
        val uv = Uuid.fromSlice(list.toByteArray()).getOrThrow()
        assertEquals(uv, u)
    }

    @Test
    fun testBytesRoundtrip() {
        val bIn =
            byteArrayOf(
                0xa1.toByte(), 0xa2.toByte(), 0xa3.toByte(), 0xa4.toByte(),
                0xb1.toByte(), 0xb2.toByte(), 0xc1.toByte(), 0xc2.toByte(),
                0xd1.toByte(), 0xd2.toByte(), 0xd3.toByte(), 0xd4.toByte(),
                0xd5.toByte(), 0xd6.toByte(), 0xd7.toByte(), 0xd8.toByte(),
            )

        val u = Uuid.fromSlice(bIn).getOrThrow()
        val bOut = u.asBytes()

        assertContentEquals(bIn, bOut)
    }

    @Test
    fun testBytesLeRoundtrip() {
        val b =
            byteArrayOf(
                0xa1.toByte(), 0xa2.toByte(), 0xa3.toByte(), 0xa4.toByte(),
                0xb1.toByte(), 0xb2.toByte(), 0xc1.toByte(), 0xc2.toByte(),
                0xd1.toByte(), 0xd2.toByte(), 0xd3.toByte(), 0xd4.toByte(),
                0xd5.toByte(), 0xd6.toByte(), 0xd7.toByte(), 0xd8.toByte(),
            )

        val u1 = Uuid.fromBytes(b)
        val bLe = u1.toBytesLe()
        val u2 = Uuid.fromBytesLe(bLe)

        assertEquals(u1, u2)
    }

    @Test
    fun testIterbytesImplForUuid() {
        val set = mutableSetOf<Uuid>()
        val id1 = new()
        val id2 = new2()
        set.add(id1)

        assertTrue(id1 in set)
        assertFalse(id2 in set)
    }
}

private fun ULong.reverseBytes(): ULong {
    val b0 = (this and 0x00000000000000ffuL) shl 56
    val b1 = (this and 0x000000000000ff00uL) shl 40
    val b2 = (this and 0x0000000000ff0000uL) shl 24
    val b3 = (this and 0x00000000ff000000uL) shl 8
    val b4 = (this and 0x000000ff00000000uL) shr 8
    val b5 = (this and 0x0000ff0000000000uL) shr 24
    val b6 = (this and 0x00ff000000000000uL) shr 40
    val b7 = (this and 0xff00000000000000uL) shr 56
    return b0 or b1 or b2 or b3 or b4 or b5 or b6 or b7
}

private fun UInt.reverseBytes(): UInt =
    ((this and 0x000000ffu) shl 24) or
        ((this and 0x0000ff00u) shl 8) or
        ((this and 0x00ff0000u) shr 8) or
        ((this and 0xff000000u) shr 24)

private fun UShort.reverseBytes(): UShort {
    val value = toUInt()
    return (((value and 0x00ffu) shl 8) or ((value and 0xff00u) shr 8)).toUShort()
}

internal fun new(): Uuid =
    Uuid.fromBytes(
        byteArrayOf(
            0xF9.toByte(),
            0x16,
            0x8C.toByte(),
            0x5E,
            0xCE.toByte(),
            0xB2.toByte(),
            0x4F,
            0xAA.toByte(),
            0xB6.toByte(),
            0xBF.toByte(),
            0x32,
            0x9B.toByte(),
            0xF3.toByte(),
            0x9F.toByte(),
            0xA1.toByte(),
            0xE4.toByte(),
        ),
    )

internal fun new2(): Uuid =
    Uuid.fromBytes(
        byteArrayOf(
            0xF9.toByte(),
            0x16,
            0x8C.toByte(),
            0x5E,
            0xCE.toByte(),
            0xB2.toByte(),
            0x4F,
            0xAB.toByte(),
            0xB6.toByte(),
            0xBF.toByte(),
            0x32,
            0x9B.toByte(),
            0xF3.toByte(),
            0x9F.toByte(),
            0xA1.toByte(),
            0xE4.toByte(),
        ),
    )

internal fun fixture(): Uuid = new()
