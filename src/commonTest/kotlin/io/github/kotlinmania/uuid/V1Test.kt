// port-lint: tests v1.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class V1Test {
    @Test
    fun testNew() {
        val time = 1_496_854_535uL
        val timeFraction = 812_946_000u
        val node = byteArrayOf(1, 2, 3, 4, 5, 6)
        val context = Context(0u)

        val uuid = Uuid.newV1(Timestamp.fromUnix(context, time, timeFraction), node)

        assertEquals(Version.Mac, uuid.getVersion())
        assertEquals(Variant.RFC4122, uuid.getVariant())
        assertEquals(
            "20616934-4ba2-11e7-8000-010203040506",
            uuid.hyphenated().toString(),
        )

        val ts = uuid.getTimestamp()!!.toGregorian()
        assertEquals(14_968_545_358_129_460uL, ts.first - UUID_TICKS_BETWEEN_EPOCHS)

        assertContentEquals(node, uuid.getNodeId())

        val parsed = Uuid.parseStr("20616934-4ba2-11e7-8000-010203040506")
        assertEquals(uuid.getTimestamp(), parsed.getTimestamp())
        assertContentEquals(uuid.getNodeId(), parsed.getNodeId())
    }

    @Test
    fun testNow() {
        val node = byteArrayOf(1, 2, 3, 4, 5, 6)
        val uuid = Uuid.nowV1(node)

        assertEquals(Version.Mac, uuid.getVersion())
        assertEquals(Variant.RFC4122, uuid.getVariant())
    }
}
