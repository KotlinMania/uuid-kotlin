// port-lint: source uuid/src/lib.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.uuid

import kotlin.native.HiddenFromObjC

/**
 * The version of the UUID, denoting the generating algorithm.
 */
public enum class Version(
    public val number: Int,
) {
    /**
     * The nil, all-zero UUID.
     */
    Nil(0),

    /**
     * Version 1: timestamp and node ID.
     */
    Mac(1),

    /**
     * Version 2: DCE Security.
     */
    Dce(2),

    /**
     * Version 3: MD5 hash.
     */
    Md5(3),

    /**
     * Version 4: random.
     */
    Random(4),

    /**
     * Version 5: SHA-1 hash.
     */
    Sha1(5),

    /**
     * Version 6: sortable timestamp and node ID.
     */
    SortMac(6),

    /**
     * Version 7: timestamp and random.
     */
    SortRand(7),

    /**
     * Version 8: custom.
     */
    Custom(8),

    /**
     * The max, all-ones UUID.
     */
    Max(0xff),
}

/**
 * The reserved variants of UUIDs.
 */
public enum class Variant {
    /**
     * Reserved by the NCS for backward compatibility.
     */
    NCS,

    /**
     * As described in RFC 9562.
     */
    RFC4122,

    /**
     * Reserved by Microsoft for backward compatibility.
     */
    Microsoft,

    /**
     * Reserved for future expansion.
     */
    Future,
}

/**
 * A Universally Unique Identifier.
 */
public class Uuid private constructor(
    private val bytes: ByteArray,
) : Comparable<Uuid> {
    /**
     * Returns the variant of the UUID structure.
     */
    public fun getVariant(): Variant {
        val x = bytes[8].toInt() and 0xff
        return when {
            x and 0x80 == 0x00 -> Variant.NCS
            x and 0xc0 == 0x80 -> Variant.RFC4122
            x and 0xe0 == 0xc0 -> Variant.Microsoft
            else -> Variant.Future
        }
    }

    /**
     * Returns the version number of the UUID.
     */
    public fun getVersionNum(): Int = (bytes[6].toInt() and 0xff) ushr 4

    /**
     * Returns the version of the UUID, if the version field is recognized.
     */
    public fun getVersion(): Version? =
        when (getVersionNum()) {
            0 -> if (isNil()) Version.Nil else null
            1 -> Version.Mac
            2 -> Version.Dce
            3 -> Version.Md5
            4 -> Version.Random
            5 -> Version.Sha1
            6 -> Version.SortMac
            7 -> Version.SortRand
            8 -> Version.Custom
            0xf -> Version.Max
            else -> null
        }

    /**
     * If the UUID is the correct version (v1, v6, or v7) this will return the
     * timestamp and counter used to create it. For other versions, or if the timestamp
     * cannot be decoded, this will return null.
     */
    public fun getTimestamp(): Timestamp? =
        when (getVersion()) {
            Version.Mac -> {
                val (ticks, counter) = decodeGregorianTimestamp(this)
                Timestamp.fromGregorian(ticks, counter)
            }
            Version.SortMac -> {
                val (ticks, counter) = decodeSortedGregorianTimestamp(this)
                Timestamp.fromGregorian(ticks, counter)
            }
            Version.SortRand -> {
                val millis = decodeUnixTimestampMillis(this)
                val seconds = millis / 1000uL
                val nanos = ((millis % 1000uL) * 1_000_000uL).toUInt()
                Timestamp.fromUnixTime(seconds, nanos, 0uL, 0u)
            }
            else -> null
        }

    /**
     * If the UUID is the correct version (v1 or v6) this will return the
     * node value as a 6-byte array. For other versions this will return null.
     */
    public fun getNodeId(): ByteArray? =
        when (getVersion()) {
            Version.Mac, Version.SortMac -> bytes.copyOfRange(10, 16)
            else -> null
        }

    /**
     * Returns the four field values of the UUID.
     */
    public fun asFields(): UuidFields {
        val d1 =
            ((bytes[0].toInt() and 0xff).toUInt() shl 24) or
                ((bytes[1].toInt() and 0xff).toUInt() shl 16) or
                ((bytes[2].toInt() and 0xff).toUInt() shl 8) or
                (bytes[3].toInt() and 0xff).toUInt()
        val d2 =
            (((bytes[4].toInt() and 0xff) shl 8) or (bytes[5].toInt() and 0xff)).toUShort()
        val d3 =
            (((bytes[6].toInt() and 0xff) shl 8) or (bytes[7].toInt() and 0xff)).toUShort()
        return UuidFields(d1, d2, d3, bytes.copyOfRange(8, 16))
    }

    /**
     * Returns the four field values of the UUID in little-endian order.
     */
    public fun toFieldsLe(): UuidFields {
        val d1 =
            (bytes[0].toInt() and 0xff).toUInt() or
                ((bytes[1].toInt() and 0xff).toUInt() shl 8) or
                ((bytes[2].toInt() and 0xff).toUInt() shl 16) or
                ((bytes[3].toInt() and 0xff).toUInt() shl 24)
        val d2 =
            ((bytes[4].toInt() and 0xff) or ((bytes[5].toInt() and 0xff) shl 8)).toUShort()
        val d3 =
            ((bytes[6].toInt() and 0xff) or ((bytes[7].toInt() and 0xff) shl 8)).toUShort()
        return UuidFields(d1, d2, d3, bytes.copyOfRange(8, 16))
    }

    /**
     * Returns two 64-bit values containing the UUID value.
     */
    public fun asU64Pair(): Pair<ULong, ULong> =
        readU64(0) to readU64(8)

    /**
     * Returns the 128-bit integer value in big-endian order as a (high, low) 64-bit word pair.
     */
    public fun asU128(): Pair<ULong, ULong> = asU64Pair()

    /**
     * Returns the 128-bit integer value in little-endian order as a (high, low) 64-bit word pair.
     */
    public fun toU128Le(): Pair<ULong, ULong> {
        val low = readU64Le(bytes, 0)
        val high = readU64Le(bytes, 8)
        return high to low
    }

    /**
     * Returns a copy of the 16 octets containing the UUID value.
     */
    public fun asBytes(): ByteArray = bytes.copyOf()

    internal fun rawBytes(): ByteArray = bytes

    /**
     * Consumes the UUID value and returns a copy of its bytes.
     */
    public fun intoBytes(): ByteArray = asBytes()

    /**
     * Returns the bytes of the UUID in little-endian field order.
     */
    public fun toBytesLe(): ByteArray =
        byteArrayOf(
            bytes[3],
            bytes[2],
            bytes[1],
            bytes[0],
            bytes[5],
            bytes[4],
            bytes[7],
            bytes[6],
            bytes[8],
            bytes[9],
            bytes[10],
            bytes[11],
            bytes[12],
            bytes[13],
            bytes[14],
            bytes[15],
        )

    /**
     * Tests if the UUID is nil, meaning all bytes are zero.
     */
    public fun isNil(): Boolean = bytes.all { it == 0.toByte() }

    /**
     * Tests if the UUID is max, meaning all bytes are one.
     */
    public fun isMax(): Boolean = bytes.all { (it.toInt() and 0xff) == 0xff }

    override fun compareTo(other: Uuid): Int {
        for (i in bytes.indices) {
            val left = bytes[i].toInt() and 0xff
            val right = other.bytes[i].toInt() and 0xff
            if (left != right) return left - right
        }
        return 0
    }

    override fun equals(other: Any?): Boolean =
        other is Uuid && bytes.contentEquals(other.bytes)

    /**
     * Compares two UUIDs for equality.
     */
    public fun eq(other: Uuid): Boolean = this == other

    /**
     * Computes the hash code of the UUID.
     */
    public fun hash(): Int = hashCode()

    /**
     * Returns the UUID byte slice.
     */
    public fun asRef(): ByteArray = asBytes()

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun toString(): String = hyphenated().toString()

    private fun readU64(offset: Int): ULong {
        var value = 0UL
        for (i in 0 until 8) {
            value = (value shl 8) or (bytes[offset + i].toInt() and 0xff).toULong()
        }
        return value
    }

    private fun readU64Le(data: ByteArray, offset: Int): ULong {
        var value = 0UL
        for (i in 7 downTo 0) {
            value = (value shl 8) or (data[offset + i].toInt() and 0xff).toULong()
        }
        return value
    }

    public companion object {
        /**
         * The nil UUID, with all bits set to zero.
         */
        public val NIL: Uuid = fromBytes(ByteArray(16))

        /**
         * The max UUID, with all bits set to one.
         */
        public val MAX: Uuid = fromBytes(ByteArray(16) { 0xFF.toByte() })

        /**
         * UUID namespace for Domain Name System.
         */
        public val NAMESPACE_DNS: Uuid =
            fromBytes(
                byteArrayOf(
                    0x6b,
                    0xa7.toByte(),
                    0xb8.toByte(),
                    0x10,
                    0x9d.toByte(),
                    0xad.toByte(),
                    0x11,
                    0xd1.toByte(),
                    0x80.toByte(),
                    0xb4.toByte(),
                    0x00,
                    0xc0.toByte(),
                    0x4f,
                    0xd4.toByte(),
                    0x30,
                    0xc8.toByte(),
                ),
            )

        /**
         * UUID namespace for ISO Object Identifiers.
         */
        public val NAMESPACE_OID: Uuid =
            fromBytes(
                byteArrayOf(
                    0x6b,
                    0xa7.toByte(),
                    0xb8.toByte(),
                    0x12,
                    0x9d.toByte(),
                    0xad.toByte(),
                    0x11,
                    0xd1.toByte(),
                    0x80.toByte(),
                    0xb4.toByte(),
                    0x00,
                    0xc0.toByte(),
                    0x4f,
                    0xd4.toByte(),
                    0x30,
                    0xc8.toByte(),
                ),
            )

        /**
         * UUID namespace for Uniform Resource Locators.
         */
        public val NAMESPACE_URL: Uuid =
            fromBytes(
                byteArrayOf(
                    0x6b,
                    0xa7.toByte(),
                    0xb8.toByte(),
                    0x11,
                    0x9d.toByte(),
                    0xad.toByte(),
                    0x11,
                    0xd1.toByte(),
                    0x80.toByte(),
                    0xb4.toByte(),
                    0x00,
                    0xc0.toByte(),
                    0x4f,
                    0xd4.toByte(),
                    0x30,
                    0xc8.toByte(),
                ),
            )

        /**
         * UUID namespace for X.500 Distinguished Names.
         */
        public val NAMESPACE_X500: Uuid =
            fromBytes(
                byteArrayOf(
                    0x6b,
                    0xa7.toByte(),
                    0xb8.toByte(),
                    0x14,
                    0x9d.toByte(),
                    0xad.toByte(),
                    0x11,
                    0xd1.toByte(),
                    0x80.toByte(),
                    0xb4.toByte(),
                    0x00,
                    0xc0.toByte(),
                    0x4f,
                    0xd4.toByte(),
                    0x30,
                    0xc8.toByte(),
                ),
            )

        /**
         * Parses a UUID from a string of hexadecimal digits with optional hyphens.
         */
        public fun parseStr(input: String): Uuid =
            parseUuidBytes(input.encodeToByteArray()).fold(
                onSuccess = { fromBytes(it) },
                onFailure = { failure ->
                    throw if (failure is InvalidUuid) failure.intoErr() else failure
                },
            )

        /**
         * Parses a UUID from a string of hexadecimal digits with optional hyphens.
         */
        @HiddenFromObjC
        public fun tryParse(input: String): Result<Uuid> = tryParseAscii(input.encodeToByteArray())

        /**
         * Parses a UUID from ASCII bytes containing hexadecimal digits with optional hyphens.
         */
        @HiddenFromObjC
        public fun tryParseAscii(input: ByteArray): Result<Uuid> =
            parseUuidBytes(input).fold(
                onSuccess = { Result.success(fromBytes(it)) },
                onFailure = { Result.failure(Error(ErrorKind.ParseOther)) },
            )

        /**
         * Returns the default nil UUID.
         */
        public fun default(): Uuid = NIL

        /**
         * Creates a UUID from 16 bytes.
         */
        public fun from(bytes: ByteArray): Uuid = fromBytes(bytes)

        /**
         * Attempts to create a UUID from a byte slice.
         */
        @HiddenFromObjC
        public fun tryFrom(bytes: ByteArray): Result<Uuid> = fromSlice(bytes)

        /**
         * Constructs a UUID from 16 bytes.
         */
        public fun new(bytes: ByteArray): Uuid = fromBytes(bytes)

        /**
         * Constructs a UUID from two 64-bit unsigned integers.
         */
        public fun new2(d1: ULong, d2: ULong): Uuid = fromU64Pair(d1, d2)

        /**
         * Creates a UUID from 16 bytes.
         */
        public fun fromBytes(bytes: ByteArray): Uuid {
            require(bytes.size == UUID_LENGTH) { "expected 16 bytes, found ${bytes.size}" }
            return Uuid(bytes.copyOf())
        }

        /**
         * Creates a UUID from 16 bytes in little-endian field order.
         */
        public fun fromBytesLe(bytes: ByteArray): Uuid {
            require(bytes.size == UUID_LENGTH) { "expected 16 bytes, found ${bytes.size}" }
            return fromBytes(
                byteArrayOf(
                    bytes[3],
                    bytes[2],
                    bytes[1],
                    bytes[0],
                    bytes[5],
                    bytes[4],
                    bytes[7],
                    bytes[6],
                    bytes[8],
                    bytes[9],
                    bytes[10],
                    bytes[11],
                    bytes[12],
                    bytes[13],
                    bytes[14],
                    bytes[15],
                ),
            )
        }

        /**
         * Creates a UUID from a byte slice.
         */
        @HiddenFromObjC
        public fun fromSlice(bytes: ByteArray): Result<Uuid> =
            if (bytes.size == UUID_LENGTH) {
                Result.success(fromBytes(bytes))
            } else {
                Result.failure(Error(ErrorKind.ParseByteLength(bytes.size)))
            }

        /**
         * Creates a UUID from its four field values.
         */
        public fun fromFields(
            d1: UInt,
            d2: UShort,
            d3: UShort,
            d4: ByteArray,
        ): Uuid {
            require(d4.size == 8) { "expected 8 bytes in the final UUID field, found ${d4.size}" }
            return fromBytes(
                byteArrayOf(
                    ((d1 shr 24) and 0xffu).toByte(),
                    ((d1 shr 16) and 0xffu).toByte(),
                    ((d1 shr 8) and 0xffu).toByte(),
                    (d1 and 0xffu).toByte(),
                    ((d2.toUInt() shr 8) and 0xffu).toByte(),
                    (d2.toUInt() and 0xffu).toByte(),
                    ((d3.toUInt() shr 8) and 0xffu).toByte(),
                    (d3.toUInt() and 0xffu).toByte(),
                    d4[0],
                    d4[1],
                    d4[2],
                    d4[3],
                    d4[4],
                    d4[5],
                    d4[6],
                    d4[7],
                ),
            )
        }

        /**
         * Creates a UUID from its four field values in little-endian order.
         */
        public fun fromFieldsLe(
            d1: UInt,
            d2: UShort,
            d3: UShort,
            d4: ByteArray,
        ): Uuid =
            fromFields(
                d1.reverseBytes(),
                d2.reverseBytes(),
                d3.reverseBytes(),
                d4,
            )

        /**
         * Creates a UUID from two 64-bit values.
         */
        public fun fromU64Pair(high: ULong, low: ULong): Uuid {
            val out = ByteArray(UUID_LENGTH)
            writeU64(out, 0, high)
            writeU64(out, 8, low)
            return fromBytes(out)
        }

        /**
         * Creates a UUID from a 128-bit value (high and low 64-bit words).
         */
        public fun fromU128(high: ULong, low: ULong): Uuid = fromU64Pair(high, low)

        /**
         * Creates a UUID from a 128-bit value in little-endian order.
         */
        public fun fromU128Le(high: ULong, low: ULong): Uuid {
            val out = ByteArray(UUID_LENGTH)
            writeU64Le(out, 0, low)
            writeU64Le(out, 8, high)
            return fromBytes(out)
        }

        /**
         * Returns the nil UUID.
         */
        public fun nil(): Uuid = fromBytes(ByteArray(UUID_LENGTH))

        /**
         * Returns the max UUID.
         */
        public fun max(): Uuid = fromBytes(ByteArray(UUID_LENGTH) { 0xff.toByte() })

        /**
         * Returns a buffer long enough for any format adapter.
         */
        public fun encodeBuffer(): ByteArray = ByteArray(Urn.LENGTH)

        private fun writeU64(
            target: ByteArray,
            offset: Int,
            value: ULong,
        ) {
            for (i in 0 until 8) {
                val shift = (7 - i) * 8
                target[offset + i] = ((value shr shift) and 0xffu).toByte()
            }
        }

        private fun writeU64Le(
            target: ByteArray,
            offset: Int,
            value: ULong,
        ) {
            for (i in 0 until 8) {
                val shift = i * 8
                target[offset + i] = ((value shr shift) and 0xffu).toByte()
            }
        }
    }
}

/**
 * The UUID fields returned by [Uuid.asFields] and [Uuid.toFieldsLe].
 */
public data class UuidFields(
    public val d1: UInt,
    public val d2: UShort,
    public val d3: UShort,
    public val d4: ByteArray,
) {
    override fun equals(other: Any?): Boolean =
        other is UuidFields &&
            d1 == other.d1 &&
            d2 == other.d2 &&
            d3 == other.d3 &&
            d4.contentEquals(other.d4)

    override fun hashCode(): Int {
        var result = d1.hashCode()
        result = 31 * result + d2.hashCode()
        result = 31 * result + d3.hashCode()
        result = 31 * result + d4.contentHashCode()
        return result
    }
}

private const val UUID_LENGTH = 16

private fun UInt.reverseBytes(): UInt =
    ((this and 0x000000ffu) shl 24) or
        ((this and 0x0000ff00u) shl 8) or
        ((this and 0x00ff0000u) shr 8) or
        ((this and 0xff000000u) shr 24)

private fun UShort.reverseBytes(): UShort {
    val value = toUInt()
    return (((value and 0x00ffu) shl 8) or ((value and 0xff00u) shr 8)).toUShort()
}

public typealias Bytes = ByteArray
