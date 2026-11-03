// port-lint: source v7.rs
package io.github.kotlinmania.uuid

import kotlin.random.Random

/**
 * Creates a new version 7 UUID using the current time value.
 */
public fun Uuid.Companion.nowV7(): Uuid =
    newV7(Timestamp.now(Timestamp.sharedContextV7()))

/**
 * Creates a new version 7 UUID using a time value and random bytes.
 */
public fun Uuid.Companion.newV7(ts: Timestamp): Uuid {
    val (secs, nanos) = ts.toUnix()
    val millis = (secs * 1000uL) + (nanos.toULong() / 1_000_000uL)

    val raw = Random.nextBytes(10)
    var (counter, counterBits) = ts.counter()
    var cBits = counterBits.toInt()

    if (cBits > 12) {
        val lowBits = cBits - 12
        val mask = (1uL shl lowBits) - 1uL
        val high12 = (counter shr lowBits) and 0x0FFFuL
        val low = counter and mask
        counter = (high12 shl (lowBits + 2)) or low
        cBits += 2
    }

    if (cBits > 0) {
        var top64 = 0uL
        for (i in 0 until 8) {
            top64 = (top64 shl 8) or (raw[i].toLong() and 0xFF).toULong()
        }

        val shift = 64 - cBits
        val mask = if (shift >= 64) 0uL else (ULong.MAX_VALUE shr cBits)
        top64 = (top64 and mask) or (counter shl shift)

        for (i in 0 until 8) {
            val s = (7 - i) * 8
            raw[i] = ((top64 shr s) and 0xFFuL).toByte()
        }
    }

    return Builder.fromUnixTimestampMillis(millis, raw).intoUuid()
}
