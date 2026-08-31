// port-lint: tests uuid/src/v7.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class V7Test {
    @Test
    fun testNew() {
        val ts: ULong = 1645557742000uL

        val seconds = ts / 1000uL
        val nanos = ((ts % 1000uL) * 1_000_000uL).toUInt()

        val uuid = Uuid.newV7(Timestamp.fromUnix(NoContext, seconds, nanos))
        val uustr = uuid.hyphenated().toString()

        assertEquals(Version.SortRand, uuid.getVersion())
        assertEquals(Variant.RFC4122, uuid.getVariant())
        assertTrue(uuid.hyphenated().toString().startsWith("017f22e2-79b0-7"))

        val parsed = Uuid.parseStr(uustr)
        assertEquals(uuid, parsed)
    }

    @Test
    fun testNow() {
        val uuid = Uuid.nowV7()

        assertEquals(Version.SortRand, uuid.getVersion())
        assertEquals(Variant.RFC4122, uuid.getVariant())
    }

    @Test
    fun testSorting() {
        val time1 = 1_496_854_535uL
        val timeFraction1 = 812_000_000u

        val time2 = time1 + 4000uL
        val timeFraction2 = timeFraction1

        val uuid1 = Uuid.newV7(Timestamp.fromUnix(NoContext, time1, timeFraction1))
        val uuid2 = Uuid.newV7(Timestamp.fromUnix(NoContext, time2, timeFraction2))

        assertTrue(uuid1 < uuid2)
        assertTrue(uuid1.toString() < uuid2.toString())
    }

    @Test
    fun testNewTimestampRoundtrip() {
        val time = 1_496_854_535uL
        val timeFraction = 812_000_000u

        val ts = Timestamp.fromUnix(NoContext, time, timeFraction)

        val uuid = Uuid.newV7(ts)

        val decodedTs = uuid.getTimestamp()!!

        assertEquals(ts.toUnix(), decodedTs.toUnix())
    }

    private class MaxContext : ClockSequence<ULong> {
        override fun generateSequence(seconds: ULong, subsecNanos: UInt): ULong = ULong.MAX_VALUE

        override fun usableBits(): Int = 64
    }

    @Test
    fun testNewMaxContext() {
        val maxContext = MaxContext()

        val time = 1_496_854_535uL
        val timeFraction = 812_000_000u

        val ts = Timestamp.fromUnix(maxContext, time, timeFraction)

        val uuid = Uuid.newV7(ts)

        assertEquals(Version.SortRand, uuid.getVersion())
        assertEquals(Variant.RFC4122, uuid.getVariant())

        val decodedTs = uuid.getTimestamp()!!

        assertEquals(ts.toUnix(), decodedTs.toUnix())
    }
}
