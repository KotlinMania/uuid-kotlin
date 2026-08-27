// port-lint: source fmt.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.uuid

import kotlin.native.HiddenFromObjC

/**
 * Format a [Uuid] as a hyphenated string, like
 * `67e55044-10b1-426f-9247-bb680e5fe0c8`.
 */
public class Hyphenated internal constructor(
    private val uuid: Uuid,
) {
    public fun encodeLower(buffer: ByteArray): String = encodeHyphenated(uuid.rawBytes(), buffer, upper = false)

    public fun encodeUpper(buffer: ByteArray): String = encodeHyphenated(uuid.rawBytes(), buffer, upper = true)

    public fun asUuid(): Uuid = uuid

    public fun intoUuid(): Uuid = uuid

    override fun equals(other: Any?): Boolean = other is Hyphenated && uuid == other.uuid

    override fun hashCode(): Int = uuid.hashCode()

    override fun toString(): String = encodeLower(ByteArray(LENGTH))

    public companion object {
        /**
         * The length of a hyphenated UUID string.
         */
        public const val LENGTH: Int = 36

        /**
         * Creates a [Hyphenated] formatter from a [Uuid].
         */
        public fun fromUuid(uuid: Uuid): Hyphenated = Hyphenated(uuid)

        /**
         * Creates a [Hyphenated] formatter from a [Uuid].
         */
        public fun from(uuid: Uuid): Hyphenated = Hyphenated(uuid)

        /**
         * Parses a hyphenated UUID formatter from a string.
         */
        @HiddenFromObjC
        public fun fromStr(input: String): Result<Hyphenated> = parse(input)

        /**
         * Parses a hyphenated UUID formatter.
         */
        @HiddenFromObjC
        public fun parse(input: String): Result<Hyphenated> =
            parseHyphenated(input.encodeToByteArray()).map { Hyphenated(Uuid.fromBytes(it)) }
    }
}

/**
 * Format a [Uuid] as a simple string, like
 * `67e5504410b1426f9247bb680e5fe0c8`.
 */
public class Simple internal constructor(
    private val uuid: Uuid,
) {
    public fun encodeLower(buffer: ByteArray): String = encodeSimple(uuid.rawBytes(), buffer, upper = false)

    public fun encodeUpper(buffer: ByteArray): String = encodeSimple(uuid.rawBytes(), buffer, upper = true)

    public fun asUuid(): Uuid = uuid

    public fun intoUuid(): Uuid = uuid

    override fun equals(other: Any?): Boolean = other is Simple && uuid == other.uuid

    override fun hashCode(): Int = uuid.hashCode()

    override fun toString(): String = encodeLower(ByteArray(LENGTH))

    public companion object {
        /**
         * The length of a simple UUID string.
         */
        public const val LENGTH: Int = 32

        /**
         * Creates a [Simple] formatter from a [Uuid].
         */
        public fun fromUuid(uuid: Uuid): Simple = Simple(uuid)

        /**
         * Creates a [Simple] formatter from a [Uuid].
         */
        public fun from(uuid: Uuid): Simple = Simple(uuid)

        /**
         * Parses a simple UUID formatter from a string.
         */
        @HiddenFromObjC
        public fun fromStr(input: String): Result<Simple> = parse(input)

        /**
         * Parses a simple UUID formatter.
         */
        @HiddenFromObjC
        public fun parse(input: String): Result<Simple> =
            parseSimple(input.encodeToByteArray()).map { Simple(Uuid.fromBytes(it)) }
    }
}

/**
 * Format a [Uuid] as a URN string, like
 * `urn:uuid:67e55044-10b1-426f-9247-bb680e5fe0c8`.
 */
public class Urn internal constructor(
    private val uuid: Uuid,
) {
    public fun encodeLower(buffer: ByteArray): String = encodeUrn(uuid.rawBytes(), buffer, upper = false)

    public fun encodeUpper(buffer: ByteArray): String = encodeUrn(uuid.rawBytes(), buffer, upper = true)

    public fun asUuid(): Uuid = uuid

    public fun intoUuid(): Uuid = uuid

    override fun equals(other: Any?): Boolean = other is Urn && uuid == other.uuid

    override fun hashCode(): Int = uuid.hashCode()

    override fun toString(): String = encodeLower(ByteArray(LENGTH))

    public companion object {
        /**
         * The length of a URN UUID string.
         */
        public const val LENGTH: Int = 45

        /**
         * Creates a [Urn] formatter from a [Uuid].
         */
        public fun fromUuid(uuid: Uuid): Urn = Urn(uuid)

        /**
         * Creates a [Urn] formatter from a [Uuid].
         */
        public fun from(uuid: Uuid): Urn = Urn(uuid)

        /**
         * Parses a URN UUID formatter from a string.
         */
        @HiddenFromObjC
        public fun fromStr(input: String): Result<Urn> = parse(input)

        /**
         * Parses a URN UUID formatter.
         */
        @HiddenFromObjC
        public fun parse(input: String): Result<Urn> =
            parseUrn(input.encodeToByteArray()).map { Urn(Uuid.fromBytes(it)) }
    }
}

/**
 * Format a [Uuid] as a braced hyphenated string, like
 * `{67e55044-10b1-426f-9247-bb680e5fe0c8}`.
 */
public class Braced internal constructor(
    private val uuid: Uuid,
) {
    public fun encodeLower(buffer: ByteArray): String = encodeBraced(uuid.rawBytes(), buffer, upper = false)

    public fun encodeUpper(buffer: ByteArray): String = encodeBraced(uuid.rawBytes(), buffer, upper = true)

    public fun asUuid(): Uuid = uuid

    public fun intoUuid(): Uuid = uuid

    override fun equals(other: Any?): Boolean = other is Braced && uuid == other.uuid

    override fun hashCode(): Int = uuid.hashCode()

    override fun toString(): String = encodeLower(ByteArray(LENGTH))

    public companion object {
        /**
         * The length of a braced UUID string.
         */
        public const val LENGTH: Int = 38

        /**
         * Creates a [Braced] formatter from a [Uuid].
         */
        public fun fromUuid(uuid: Uuid): Braced = Braced(uuid)

        /**
         * Creates a [Braced] formatter from a [Uuid].
         */
        public fun from(uuid: Uuid): Braced = Braced(uuid)

        /**
         * Parses a braced UUID formatter from a string.
         */
        @HiddenFromObjC
        public fun fromStr(input: String): Result<Braced> = parse(input)

        /**
         * Parses a braced UUID formatter.
         */
        @HiddenFromObjC
        public fun parse(input: String): Result<Braced> =
            parseBraced(input.encodeToByteArray()).map { Braced(Uuid.fromBytes(it)) }
    }
}

/**
 * Gets a [Hyphenated] formatter.
 */
public fun Uuid.hyphenated(): Hyphenated = Hyphenated(this)

/**
 * Gets a borrowed [Hyphenated] formatter.
 */
public fun Uuid.asHyphenated(): Hyphenated = Hyphenated(this)

/**
 * Gets a [Simple] formatter.
 */
public fun Uuid.simple(): Simple = Simple(this)

/**
 * Gets a borrowed [Simple] formatter.
 */
public fun Uuid.asSimple(): Simple = Simple(this)

/**
 * Gets a [Urn] formatter.
 */
public fun Uuid.urn(): Urn = Urn(this)

/**
 * Gets a borrowed [Urn] formatter.
 */
public fun Uuid.asUrn(): Urn = Urn(this)

/**
 * Gets a [Braced] formatter.
 */
public fun Uuid.braced(): Braced = Braced(this)

/**
 * Gets a borrowed [Braced] formatter.
 */
public fun Uuid.asBraced(): Braced = Braced(this)

private val UPPER: CharArray = "0123456789ABCDEF".toCharArray()
private val LOWER: CharArray = "0123456789abcdef".toCharArray()

private fun encodeSimple(
    src: ByteArray,
    buffer: ByteArray,
    upper: Boolean,
): String {
    require(buffer.size >= Simple.LENGTH) { "buffer must have length at least ${Simple.LENGTH}" }
    val chars = if (upper) UPPER else LOWER
    var out = 0
    for (byte in src) {
        val value = byte.toInt() and 0xff
        buffer[out] = chars[value ushr 4].code.toByte()
        buffer[out + 1] = chars[value and 0x0f].code.toByte()
        out += 2
    }
    return buffer.decodeToString(0, Simple.LENGTH)
}

private fun encodeHyphenated(
    src: ByteArray,
    buffer: ByteArray,
    upper: Boolean,
): String {
    require(buffer.size >= Hyphenated.LENGTH) { "buffer must have length at least ${Hyphenated.LENGTH}" }
    val chars = if (upper) UPPER else LOWER
    val groups = intArrayOf(4, 2, 2, 2, 6)
    var input = 0
    var out = 0
    for (groupIndex in groups.indices) {
        repeat(groups[groupIndex]) {
            val value = src[input].toInt() and 0xff
            buffer[out] = chars[value ushr 4].code.toByte()
            buffer[out + 1] = chars[value and 0x0f].code.toByte()
            input += 1
            out += 2
        }
        if (groupIndex < groups.lastIndex) {
            buffer[out] = '-'.code.toByte()
            out += 1
        }
    }
    return buffer.decodeToString(0, Hyphenated.LENGTH)
}

private fun encodeBraced(
    src: ByteArray,
    buffer: ByteArray,
    upper: Boolean,
): String {
    require(buffer.size >= Braced.LENGTH) { "buffer must have length at least ${Braced.LENGTH}" }
    buffer[0] = '{'.code.toByte()
    val inner = ByteArray(Hyphenated.LENGTH)
    encodeHyphenated(src, inner, upper)
    inner.copyInto(buffer, destinationOffset = 1)
    buffer[Braced.LENGTH - 1] = '}'.code.toByte()
    return buffer.decodeToString(0, Braced.LENGTH)
}

private fun encodeUrn(
    src: ByteArray,
    buffer: ByteArray,
    upper: Boolean,
): String {
    require(buffer.size >= Urn.LENGTH) { "buffer must have length at least ${Urn.LENGTH}" }
    val prefix = "urn:uuid:".encodeToByteArray()
    prefix.copyInto(buffer)
    val inner = ByteArray(Hyphenated.LENGTH)
    encodeHyphenated(src, inner, upper)
    inner.copyInto(buffer, destinationOffset = prefix.size)
    return buffer.decodeToString(0, Urn.LENGTH)
}

/**
 * Creates a [Uuid] from a [Hyphenated] formatter.
 */
public fun Uuid.Companion.from(hyphenated: Hyphenated): Uuid = hyphenated.intoUuid()

/**
 * Creates a [Uuid] from a [Simple] formatter.
 */
public fun Uuid.Companion.from(simple: Simple): Uuid = simple.intoUuid()

/**
 * Creates a [Uuid] from a [Urn] formatter.
 */
public fun Uuid.Companion.from(urn: Urn): Uuid = urn.intoUuid()

/**
 * Creates a [Uuid] from a [Braced] formatter.
 */
public fun Uuid.Companion.from(braced: Braced): Uuid = braced.intoUuid()

internal fun formatSimple(src: ByteArray, buffer: ByteArray, upper: Boolean = false): String =
    encodeSimple(src, buffer, upper)

internal fun formatHyphenated(src: ByteArray, buffer: ByteArray, upper: Boolean = false): String =
    encodeHyphenated(src, buffer, upper)

internal fun formatUrn(src: ByteArray, buffer: ByteArray, upper: Boolean = false): String =
    encodeUrn(src, buffer, upper)

internal fun formatBraced(src: ByteArray, buffer: ByteArray, upper: Boolean = false): String =
    encodeBraced(src, buffer, upper)

