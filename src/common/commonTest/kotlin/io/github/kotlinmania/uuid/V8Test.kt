// port-lint: tests v8.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertEquals

class V8Test {
    @Test
    fun testNew() {
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
        val uuid = Uuid.newV8(customBytes)

        assertEquals(Version.Custom, uuid.getVersion())
        assertEquals(Variant.RFC4122, uuid.getVariant())
        assertEquals("0f0e0d0c-0b0a-8908-8706-050403020100", uuid.hyphenated().toString())
    }
}
