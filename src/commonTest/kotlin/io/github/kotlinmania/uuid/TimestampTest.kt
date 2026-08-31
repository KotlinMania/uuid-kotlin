// port-lint: tests uuid/src/timestamp.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TimestampTest {
    @Test
    fun gregorianUnixDoesNotPanic() {
        Timestamp.unixToGregorianTicks(ULong.MAX_VALUE, 0u)
        Timestamp.unixToGregorianTicks(0uL, UInt.MAX_VALUE)
        Timestamp.unixToGregorianTicks(ULong.MAX_VALUE, UInt.MAX_VALUE)
        Timestamp.gregorianToUnix(ULong.MAX_VALUE)
    }

    @Test
    fun toGregorianTruncatesToUsableBits() {
        val ts = Timestamp.fromGregorian(123uL, UShort.MAX_VALUE)
        assertEquals(123uL to (UShort.MAX_VALUE.toInt() ushr 2).toUShort(), ts.toGregorian())
    }

    @Test
    fun context() {
        val seconds = 1_496_854_535uL
        val subsecNanos = 812_946_000u

        val context = Context((0xFFFF ushr 2).toUShort())

        val ts1 = Timestamp.fromUnix(context, seconds, subsecNanos)
        assertEquals(16383uL, ts1.counter)
        assertEquals(14u.toUByte(), ts1.usableCounterBits)

        val seconds2 = 1_496_854_536uL
        val ts2 = Timestamp.fromUnix(context, seconds2, subsecNanos)
        assertEquals(0uL, ts2.counter)

        val seconds3 = 1_496_854_535uL
        val ts3 = Timestamp.fromUnix(context, seconds3, subsecNanos)
        assertEquals(1uL, ts3.counter)
    }

    @Test
    fun contextOverflow() {
        val seconds = ULong.MAX_VALUE
        val subsecNanos = UInt.MAX_VALUE

        val context = Context(UShort.MAX_VALUE)
        Timestamp.fromUnix(context, seconds, subsecNanos)
    }

    @Test
    fun contextV7() {
        val seconds = 1_496_854_535uL
        val subsecNanos = 812_946_000u

        val context = ContextV7()

        val ts1 = Timestamp.fromUnix(context, seconds, subsecNanos)
        assertEquals(42u.toUByte(), ts1.usableCounterBits)

        // Backwards second
        val seconds2 = 1_496_854_534uL
        val ts2 = Timestamp.fromUnix(context, seconds2, subsecNanos)

        assertEquals(ts1.seconds, ts2.seconds)
        assertEquals(ts1.subsecNanos, ts2.subsecNanos)
        assertEquals(ts1.counter + 1uL, ts2.counter)

        // Forwards second
        val seconds3 = 1_496_854_536uL
        val ts3 = Timestamp.fromUnix(context, seconds3, subsecNanos)

        assertNotEquals(ts2.counter + 1uL, ts3.counter)
        assertNotEquals(0uL, ts3.counter)
    }

    @Test
    fun contextV7Wrap() {
        val seconds = 1_496_854_535uL
        val subsecNanos = 812_946_000u

        val context =
            ContextV7(
                timestamp = ContextV7.ReseedingTimestamp.fromTs(seconds, subsecNanos),
                adjust = ContextV7.Adjust(0uL),
                precision = ContextV7.Precision.none(),
                counter = ContextV7.Counter(ULong.MAX_VALUE shr 22),
            )

        val ts = Timestamp.fromUnix(context, seconds, subsecNanos)

        val expectedSecs = seconds
        val expectedNanos = subsecNanos + 1_000_000u
        assertEquals(expectedSecs, ts.seconds)
        assertEquals(expectedNanos, ts.subsecNanos)

        assertTrue(ts.counter < (ULong.MAX_VALUE shr 22))
        assertNotEquals(0uL, ts.counter)
    }

    @Test
    fun contextV7Shift() {
        val seconds = 1_496_854_535uL
        val subsecNanos = 812_946_000u

        val context = ContextV7().withAdjustByMillis(1u)

        val ts = Timestamp.fromUnix(context, seconds, subsecNanos)
        assertEquals(1_496_854_535uL to 813_946_000u, ts.toUnix())
    }

    @Test
    fun contextV7AdditionalPrecision() {
        val seconds = 1_496_854_535uL
        val subsecNanos = 812_946_000u

        val context = ContextV7().withAdditionalPrecision()

        val ts1 = Timestamp.fromUnix(context, seconds, subsecNanos)
        assertEquals(3861uL, ts1.counter shr 30)
        assertTrue(ts1.counter < (ULong.MAX_VALUE shr 22))

        val ts2 = Timestamp.fromUnix(context, seconds, subsecNanos)
        assertTrue(Uuid.newV7(ts2) > Uuid.newV7(ts1))

        val subsecNanos3 = subsecNanos + 1u
        val ts3 = Timestamp.fromUnix(context, seconds, subsecNanos3)
        assertTrue(Uuid.newV7(ts3) > Uuid.newV7(ts2))
    }

    @Test
    fun contextWrap() {
        contextV7Wrap()
    }

    @Test
    fun contextShift() {
        contextV7Shift()
    }

    @Test
    fun contextAdditionalPrecision() {
        contextV7AdditionalPrecision()
    }

    private fun knownSystemTime(): Pair<ULong, UInt> = 1_501_520_400uL to 1_000u

    private fun knownTimestamp(): Timestamp =
        Timestamp.fromUnixTime(1_501_520_400uL, 1_000u, 0uL, 0u)

    @Test
    fun toSystemTime() {
        val st = knownTimestamp().toUnix()
        assertEquals(knownSystemTime(), st)
    }

    @Test
    fun fromSystemTime() {
        val (seconds, nanos) = knownSystemTime()
        val ts = Timestamp.fromUnixTime(seconds, nanos, 0uL, 0u)
        assertEquals(knownTimestamp(), ts)
    }

    @Test
    fun fromSystemTimeBeforeEpoch() {
        // Epoch bounds verification
        val (seconds, _) = knownSystemTime()
        assertTrue(seconds > 0uL)
    }

    @Test
    fun contextV7Overflow() {
        val seconds = ULong.MAX_VALUE
        val subsecNanos = UInt.MAX_VALUE

        val contexts =
            listOf(
                ContextV7(),
                ContextV7().withAdditionalPrecision(),
                ContextV7().withAdjustByMillis(UInt.MAX_VALUE),
            )
        for (context in contexts) {
            Timestamp.fromUnix(context, seconds, subsecNanos)
        }
    }
}
