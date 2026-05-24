// port-lint: source parser.rs
package io.github.kotlinmania.uuid

internal fun parseUuidBytes(input: ByteArray): Result<ByteArray> =
    when {
        input.size == 32 -> parseSimple(input)
        input.size == 36 -> parseHyphenated(input)
        input.size == 38 && input.first() == OPEN_BRACE && input.last() == CLOSE_BRACE ->
            parseHyphenated(input.copyOfRange(1, input.lastIndex))
        input.size == 45 && input.startsWith(URN_PREFIX) ->
            parseHyphenated(input.copyOfRange(URN_PREFIX.size, input.size))
        else -> Result.failure(InvalidUuid(input))
    }

internal fun parseBraced(input: ByteArray): Result<ByteArray> =
    if (input.size == 38 && input.first() == OPEN_BRACE && input.last() == CLOSE_BRACE) {
        parseHyphenated(input.copyOfRange(1, input.lastIndex))
    } else {
        Result.failure(InvalidUuid(input))
    }

internal fun parseUrn(input: ByteArray): Result<ByteArray> =
    if (input.size == 45 && input.startsWith(URN_PREFIX)) {
        parseHyphenated(input.copyOfRange(URN_PREFIX.size, input.size))
    } else {
        Result.failure(InvalidUuid(input))
    }

internal fun parseSimple(input: ByteArray): Result<ByteArray> {
    if (input.size != 32) return Result.failure(InvalidUuid(input))

    val out = ByteArray(16)
    for (i in 0 until 16) {
        val high = hexValue(input[i * 2])
        val low = hexValue(input[i * 2 + 1])
        if (high < 0 || low < 0) return Result.failure(InvalidUuid(input))
        out[i] = ((high shl 4) or low).toByte()
    }
    return Result.success(out)
}

internal fun parseHyphenated(input: ByteArray): Result<ByteArray> {
    if (input.size != 36) return Result.failure(InvalidUuid(input))
    if (input[8] != HYPHEN || input[13] != HYPHEN || input[18] != HYPHEN || input[23] != HYPHEN) {
        return Result.failure(InvalidUuid(input))
    }

    val positions = intArrayOf(0, 4, 9, 14, 19, 24, 28, 32)
    val out = ByteArray(16)
    for (j in positions.indices) {
        val i = positions[j]
        val h1 = hexValue(input[i])
        val h2 = hexValue(input[i + 1])
        val h3 = hexValue(input[i + 2])
        val h4 = hexValue(input[i + 3])
        if (h1 < 0 || h2 < 0 || h3 < 0 || h4 < 0) return Result.failure(InvalidUuid(input))
        out[j * 2] = ((h1 shl 4) or h2).toByte()
        out[j * 2 + 1] = ((h3 shl 4) or h4).toByte()
    }
    return Result.success(out)
}

private val URN_PREFIX: ByteArray = "urn:uuid:".encodeToByteArray()
private const val HYPHEN: Byte = 45
private const val OPEN_BRACE: Byte = 123
private const val CLOSE_BRACE: Byte = 125

private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
    if (size < prefix.size) return false
    for (i in prefix.indices) {
        if (this[i] != prefix[i]) return false
    }
    return true
}

private fun hexValue(byte: Byte): Int {
    val value = byte.toInt() and 0xff
    return when (value) {
        in '0'.code..'9'.code -> value - '0'.code
        in 'a'.code..'f'.code -> value - 'a'.code + 10
        in 'A'.code..'F'.code -> value - 'A'.code + 10
        else -> -1
    }
}
