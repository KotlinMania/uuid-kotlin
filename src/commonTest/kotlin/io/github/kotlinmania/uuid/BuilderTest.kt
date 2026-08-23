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
}
