// port-lint: source timestamp.rs
@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.github.kotlinmania.uuid

import kotlin.time.Clock

/**
 * The number of 100-ns ticks between the UUID epoch (1582-10-15) and Unix epoch (1970-01-01).
 */
public const val UUID_TICKS_BETWEEN_EPOCHS: ULong = 0x01B21DD213814000uL

/**
 * The timestamp used in version 1, 6, and 7 UUIDs.
 */
public class Timestamp internal constructor(
    internal val seconds: ULong,
    internal val subsecNanos: UInt,
    internal val counter: ULong,
    internal val usableCounterBits: UByte,
) {
    /**
     * Get the value of the timestamp as the number of 100 nanosecond ticks since 00:00:00.00,
     * 15 October 1582 and a 14-bit counter, as used in versions 1 and 6 UUIDs.
     */
    public fun toGregorian(): Pair<ULong, UShort> =
        unixToGregorianTicks(seconds, subsecNanos) to (counter.toUShort() and 0x3FFFu)

    /**
     * Get the value of the timestamp as a Gregorian timestamp (deprecated alias).
     */
    @Deprecated("use toGregorian()", ReplaceWith("toGregorian()"))
    public fun toRfc4122(): Pair<ULong, UShort> = toGregorian()

    internal fun counter(): Pair<ULong, UByte> = counter to usableCounterBits

    /**
     * Get the value of the timestamp as a Unix timestamp, as used in version 7 UUIDs.
     */
    public fun toUnix(): Pair<ULong, UInt> = seconds to subsecNanos

    /**
     * Deprecated alias for converting nanoseconds.
     */
    @Deprecated("use toUnix()", ReplaceWith("toUnix().second"))
    public fun toUnixNanos(): UInt = subsecNanos

    override fun equals(other: Any?): Boolean =
        other is Timestamp &&
            seconds == other.seconds &&
            subsecNanos == other.subsecNanos &&
            counter == other.counter &&
            usableCounterBits == other.usableCounterBits

    override fun hashCode(): Int {
        var result = seconds.hashCode()
        result = 31 * result + subsecNanos.hashCode()
        result = 31 * result + counter.hashCode()
        result = 31 * result + usableCounterBits.hashCode()
        return result
    }

    override fun toString(): String =
        "Timestamp(seconds=$seconds, subsecNanos=$subsecNanos, counter=$counter, usableCounterBits=$usableCounterBits)"

    public companion object {
        private val sharedContextInstance = Context()
        private val sharedContextV7Instance = ContextV7()

        internal fun sharedContext(): Context = sharedContextInstance

        internal fun sharedContextV7(): ContextV7 = sharedContextV7Instance

        /**
         * Construct a timestamp from the current system time.
         */
        public fun now(context: ClockSequence<*>): Timestamp {
            val instant = Clock.System.now()
            val seconds = instant.epochSeconds.toULong()
            val subsecNanos = instant.nanosecondsOfSecond.toUInt()
            return fromUnix(context, seconds, subsecNanos)
        }

        /**
         * Construct a timestamp from Gregorian ticks and a 14-bit counter, as used in versions 1 and 6 UUIDs.
         */
        public fun fromGregorian(ticks: ULong, counter: UShort): Timestamp {
            val (seconds, subsecNanos) = gregorianToUnix(ticks)
            return Timestamp(seconds, subsecNanos, counter.toULong(), 14u)
        }

        /**
         * Creates a timestamp from RFC4122 ticks and counter (deprecated alias).
         */
        @Deprecated("use fromGregorian(ticks, counter)", ReplaceWith("fromGregorian(ticks, counter)"))
        public fun fromRfc4122(ticks: ULong, counter: UShort): Timestamp = fromGregorian(ticks, counter)

        /**
         * Construct a timestamp from a Unix timestamp and up to a 128-bit counter, as used in version 7 UUIDs.
         */
        public fun fromUnix(
            context: ClockSequence<*>,
            seconds: ULong,
            subsecNanos: UInt,
        ): Timestamp {
            val result = context.generateTimestampSequence(seconds, subsecNanos)
            val c =
                when (val value = result.counter) {
                    is ULong -> value
                    is UInt -> value.toULong()
                    is UShort -> value.toULong()
                    is UByte -> value.toULong()
                    is Number -> value.toLong().toULong()
                    else -> 0uL
                }
            return Timestamp(result.seconds, result.subsecNanos, c, context.usableBits().toUByte())
        }

        /**
         * Construct a timestamp from raw Unix timestamp components and counter.
         */
        public fun fromUnixTime(
            seconds: ULong,
            subsecNanos: UInt,
            counter: ULong,
            usableCounterBits: UByte,
        ): Timestamp = Timestamp(seconds, subsecNanos, counter, usableCounterBits)

        internal fun unixToGregorianTicks(seconds: ULong, nanos: UInt): ULong =
            UUID_TICKS_BETWEEN_EPOCHS + (seconds * 10_000_000uL) + (nanos.toULong() / 100uL)

        internal fun gregorianToUnix(ticks: ULong): Pair<ULong, UInt> {
            val diff = ticks - UUID_TICKS_BETWEEN_EPOCHS
            val seconds = diff / 10_000_000uL
            val nanos = (diff % 10_000_000uL).toUInt() * 100u
            return seconds to nanos
        }
    }
}

internal fun encodeGregorianTimestamp(
    ticks: ULong,
    counter: UShort,
    nodeId: ByteArray,
): Uuid {
    require(nodeId.size == 6) { "expected 6 bytes for node ID, found ${nodeId.size}" }
    val timeLow = (ticks and 0xFFFFFFFFuL).toUInt()
    val timeMid = ((ticks shr 32) and 0xFFFFuL).toUShort()
    val timeHighAndVersion = (((ticks shr 48) and 0x0FFFuL) or (1uL shl 12)).toUShort()

    val d4 = ByteArray(8)
    val c = counter.toInt()
    d4[0] = (((c and 0x3F00) shr 8) or 0x80).toByte()
    d4[1] = (c and 0xFF).toByte()
    d4[2] = nodeId[0]
    d4[3] = nodeId[1]
    d4[4] = nodeId[2]
    d4[5] = nodeId[3]
    d4[6] = nodeId[4]
    d4[7] = nodeId[5]

    return Uuid.fromFields(timeLow, timeMid, timeHighAndVersion, d4)
}

internal fun decodeGregorianTimestamp(uuid: Uuid): Pair<ULong, UShort> {
    val bytes = uuid.asBytes()
    val ticks =
        ((bytes[6].toLong() and 0x0F) shl 56) or
            ((bytes[7].toLong() and 0xFF) shl 48) or
            ((bytes[4].toLong() and 0xFF) shl 40) or
            ((bytes[5].toLong() and 0xFF) shl 32) or
            ((bytes[0].toLong() and 0xFF) shl 24) or
            ((bytes[1].toLong() and 0xFF) shl 16) or
            ((bytes[2].toLong() and 0xFF) shl 8) or
            (bytes[3].toLong() and 0xFF)
    val counter = (((bytes[8].toInt() and 0x3F) shl 8) or (bytes[9].toInt() and 0xFF)).toUShort()
    return ticks.toULong() to counter
}

internal fun encodeSortedGregorianTimestamp(
    ticks: ULong,
    counter: UShort,
    nodeId: ByteArray,
): Uuid {
    require(nodeId.size == 6) { "expected 6 bytes for node ID, found ${nodeId.size}" }
    val timeHigh = ((ticks shr 28) and 0xFFFFFFFFuL).toUInt()
    val timeMid = ((ticks shr 12) and 0xFFFFuL).toUShort()
    val timeLowAndVersion = ((ticks and 0x0FFFuL) or (0x6uL shl 12)).toUShort()

    val d4 = ByteArray(8)
    val c = counter.toInt()
    d4[0] = (((c and 0x3F00) shr 8) or 0x80).toByte()
    d4[1] = (c and 0xFF).toByte()
    d4[2] = nodeId[0]
    d4[3] = nodeId[1]
    d4[4] = nodeId[2]
    d4[5] = nodeId[3]
    d4[6] = nodeId[4]
    d4[7] = nodeId[5]

    return Uuid.fromFields(timeHigh, timeMid, timeLowAndVersion, d4)
}

internal fun decodeSortedGregorianTimestamp(uuid: Uuid): Pair<ULong, UShort> {
    val bytes = uuid.asBytes()
    val ticks =
        ((bytes[0].toLong() and 0xFF) shl 52) or
            ((bytes[1].toLong() and 0xFF) shl 44) or
            ((bytes[2].toLong() and 0xFF) shl 36) or
            ((bytes[3].toLong() and 0xFF) shl 28) or
            ((bytes[4].toLong() and 0xFF) shl 20) or
            ((bytes[5].toLong() and 0xFF) shl 12) or
            ((bytes[6].toLong() and 0x0F) shl 8) or
            (bytes[7].toLong() and 0xFF)
    val counter = (((bytes[8].toInt() and 0x3F) shl 8) or (bytes[9].toInt() and 0xFF)).toUShort()
    return ticks.toULong() to counter
}

internal fun encodeUnixTimestampMillis(
    millis: ULong,
    counterRandomBytes: ByteArray,
): Uuid {
    require(counterRandomBytes.size == 10) { "expected 10 bytes, found ${counterRandomBytes.size}" }
    val millisHigh = ((millis shr 16) and 0xFFFFFFFFuL).toUInt()
    val millisLow = (millis and 0xFFFFuL).toUShort()
    val b0 = counterRandomBytes[0].toUInt() and 0xFFu
    val b1 = counterRandomBytes[1].toUInt() and 0xFFu
    val counterRandomVersion = (((b0 shl 8) or b1) and 0x0FFFu or (0x7u shl 12)).toUShort()

    val d4 = ByteArray(8)
    d4[0] = ((counterRandomBytes[2].toInt() and 0x3F) or 0x80).toByte()
    for (i in 1..7) {
        d4[i] = counterRandomBytes[i + 2]
    }

    return Uuid.fromFields(millisHigh, millisLow, counterRandomVersion, d4)
}

internal fun decodeUnixTimestampMillis(uuid: Uuid): ULong {
    val bytes = uuid.asBytes()
    var millis = 0uL
    for (i in 0 until 6) {
        millis = (millis shl 8) or (bytes[i].toLong() and 0xFF).toULong()
    }
    return millis
}

/**
 * Result of generating a sequence value and potentially adjusted timestamp.
 */
public data class TimestampSequenceResult<T>(
    public val counter: T,
    public val seconds: ULong,
    public val subsecNanos: UInt,
)

/**
 * A counter that can be used by versions 1 and 6 UUIDs to support the uniqueness of timestamps.
 */
public interface ClockSequence<T> {
    /**
     * Get the next value in the sequence to feed into a timestamp.
     */
    public fun generateSequence(seconds: ULong, subsecNanos: UInt): T

    /**
     * Get the next value in the sequence, potentially also adjusting the timestamp.
     */
    public fun generateTimestampSequence(
        seconds: ULong,
        subsecNanos: UInt,
    ): TimestampSequenceResult<T> =
        TimestampSequenceResult(
            generateSequence(seconds, subsecNanos),
            seconds,
            subsecNanos,
        )

    /**
     * The number of usable bits from the least significant bit in the result of sequence generation.
     */
    public fun usableBits(): Int
}

/**
 * A thread-safe, wrapping counter that produces 14-bit values.
 */
public class Context(
    count: UShort,
) : ClockSequence<UShort> {
    private var count: Int = count.toInt() and 0x3FFF

    public constructor() : this(Rng.u16())

    override fun generateSequence(seconds: ULong, subsecNanos: UInt): UShort {
        if (seconds == 0uL && subsecNanos == 0u) {
            // no-op
        }
        val current = count
        count = (current + 1) and 0x3FFF
        return current.toUShort()
    }

    override fun usableBits(): Int = 14

    public companion object {
        public fun newRandom(): Context = Context(Rng.u16())
    }
}

/**
 * An empty counter that will always return the value 0.
 */
public object NoContext : ClockSequence<UShort> {
    override fun generateSequence(seconds: ULong, subsecNanos: UInt): UShort {
        if (seconds == 0uL && subsecNanos == 0u) {
            // no-op
        }
        return 0u
    }

    override fun usableBits(): Int = 0
}

/**
 * An unsynchronized, reseeding counter that produces 42-bit values for version 7 UUIDs.
 */
public class ContextV7(
    private var timestamp: ReseedingTimestamp = ReseedingTimestamp(),
    private var counter: Counter = Counter(0uL),
    private var adjust: Adjust = Adjust(0uL),
    private var precision: Precision = Precision.none(),
) : ClockSequence<ULong> {
    public constructor() : this(
        timestamp = ReseedingTimestamp(0uL, 0uL, 0u),
        counter = Counter(0uL),
        adjust = Adjust(0uL),
        precision = Precision.none(),
    )

    /**
     * Specify an amount to shift timestamps by to obfuscate their actual generation time.
     */
    public fun withAdjustByMillis(millis: UInt): ContextV7 {
        this.adjust = Adjust.byMillis(millis)
        return this
    }

    /**
     * Use the leftmost 12 bits of the counter for additional timestamp precision.
     */
    public fun withAdditionalPrecision(): ContextV7 {
        this.precision = Precision.withBits(12)
        return this
    }

    override fun generateSequence(seconds: ULong, subsecNanos: UInt): ULong =
        generateTimestampSequence(seconds, subsecNanos).counter

    override fun generateTimestampSequence(
        seconds: ULong,
        subsecNanos: UInt,
    ): TimestampSequenceResult<ULong> {
        val (adjSeconds, adjNanos) = adjust.apply(seconds, subsecNanos)
        val (newTimestamp, shouldReseed) = timestamp.advance(adjSeconds, adjNanos)
        var currentCounter: Counter
        var currentTs = newTimestamp

        if (shouldReseed) {
            currentCounter = Counter.reseed(precision, currentTs)
        } else {
            currentCounter = counter.increment(precision, currentTs)
            if (currentCounter.hasOverflowed()) {
                currentTs = currentTs.increment()
                currentCounter = Counter.reseed(precision, currentTs)
            }
        }

        this.timestamp = currentTs
        this.counter = currentCounter

        return TimestampSequenceResult(currentCounter.value, currentTs.seconds, currentTs.subsecNanos)
    }

    override fun usableBits(): Int = USABLE_BITS

    public companion object {
        public const val USABLE_BITS: Int = 42
        private const val RESEED_MASK: ULong = 0x000001FFFFFFFFFFuL
        private const val MAX_COUNTER: ULong = 0x000003FFFFFFFFFFuL
    }

    public data class Adjust(
        val byNs: ULong,
    ) {
        fun apply(seconds: ULong, subsecNanos: UInt): Pair<ULong, UInt> {
            if (byNs == 0uL) return seconds to subsecNanos
            val totalNs = seconds * 1_000_000_000uL + subsecNanos.toULong() + byNs
            val newSecs = totalNs / 1_000_000_000uL
            val newNanos = (totalNs % 1_000_000_000uL).toUInt()
            return newSecs to newNanos
        }

        companion object {
            fun byMillis(millis: UInt): Adjust =
                Adjust(millis.toULong() * 1_000_000uL)
        }
    }

    public data class ReseedingTimestamp(
        val lastSeed: ULong = 0uL,
        val seconds: ULong = 0uL,
        val subsecNanos: UInt = 0u,
    ) {
        fun advance(seconds: ULong, subsecNanos: UInt): Pair<ReseedingTimestamp, Boolean> {
            val incoming = fromTs(seconds, subsecNanos)
            return if (incoming.lastSeed > this.lastSeed) {
                incoming to true
            } else {
                val maxNanos = maxOf(this.subsecNanos, subsecNanos)
                ReseedingTimestamp(this.lastSeed, this.seconds, maxNanos) to false
            }
        }

        fun increment(): ReseedingTimestamp {
            val (newSecs, newNanos) = Adjust.byMillis(1u).apply(seconds, subsecNanos)
            return fromTs(newSecs, newNanos)
        }

        fun submilliNanos(): UInt = subsecNanos % 1_000_000u

        companion object {
            fun fromTs(seconds: ULong, subsecNanos: UInt): ReseedingTimestamp {
                val lastSeed = seconds * 1000uL + (subsecNanos / 1_000_000u).toULong()
                return ReseedingTimestamp(lastSeed, seconds, subsecNanos)
            }
        }
    }

    public data class Precision(
        val bits: Int,
        val factor: ULong,
        val mask: ULong,
        val shift: Int,
    ) {
        fun apply(value: ULong, timestamp: ReseedingTimestamp): ULong {
            if (bits == 0) return value
            val additional = timestamp.submilliNanos().toULong() / factor
            return (value and mask) or (additional shl shift)
        }

        companion object {
            fun none(): Precision = Precision(0, 0uL, 0uL, 0)

            fun withBits(bits: Int): Precision {
                val mask = 0xFFFFFFFFFFFFFFFFuL shr (64 - USABLE_BITS + bits)
                val shift = USABLE_BITS - bits
                val factor = (999_999uL / (1uL shl bits)) + 1uL
                return Precision(bits, factor, mask, shift)
            }
        }
    }

    public data class Counter(
        val value: ULong,
    ) {
        fun hasOverflowed(): Boolean = value > MAX_COUNTER

        fun increment(precision: Precision, timestamp: ReseedingTimestamp): Counter {
            val counter = Counter(precision.apply(value, timestamp))
            return Counter(counter.value + 1uL)
        }

        companion object {
            fun reseed(precision: Precision, timestamp: ReseedingTimestamp): Counter =
                Counter(precision.apply(Rng.u64() and RESEED_MASK, timestamp))
        }
    }
}

/**
 * A wrapper for a context that uses thread-local or shared storage.
 */
public class ThreadLocalContext<C : ClockSequence<*>>(
    private val context: C,
) : ClockSequence<Any?> {
    override fun generateSequence(seconds: ULong, subsecNanos: UInt): Any? =
        context.generateSequence(seconds, subsecNanos)

    override fun generateTimestampSequence(
        seconds: ULong,
        subsecNanos: UInt,
    ): TimestampSequenceResult<Any?> {
        @Suppress("UNCHECKED_CAST")
        val res = context.generateTimestampSequence(seconds, subsecNanos) as TimestampSequenceResult<Any?>
        return res
    }

    override fun usableBits(): Int = context.usableBits()
}

public typealias SharedContextV7 = ThreadLocalContext<ContextV7>
