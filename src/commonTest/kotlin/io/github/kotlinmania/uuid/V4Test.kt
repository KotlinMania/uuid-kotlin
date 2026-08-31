// port-lint: tests uuid/src/v4.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertEquals

class V4Test {
    @Test
    fun testNew() {
        val uuid = Uuid.newV4()

        assertEquals(Version.Random, uuid.getVersion())
        assertEquals(Variant.RFC4122, uuid.getVariant())
    }

    @Test
    fun testGetVersion() {
        val uuid = Uuid.newV4()

        assertEquals(Version.Random, uuid.getVersion())
        assertEquals(4, uuid.getVersionNum())
    }
}
