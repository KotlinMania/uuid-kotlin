// port-lint: source error.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.uuid

import kotlin.native.HiddenFromObjC

/**
 * A general error that can occur when working with UUIDs.
 */
@HiddenFromObjC
public class Error internal constructor(
    internal val kind: ErrorKind,
) : Exception() {
    override fun equals(other: Any?): Boolean = other is Error && kind == other.kind

    override fun hashCode(): Int = kind.hashCode()

    override fun toString(): String = fmt()

    internal fun fmt(): String =
        when (val current = kind) {
            is ErrorKind.ParseChar ->
                "invalid character: expected an optional prefix of `urn:uuid:` followed by [0-9a-fA-F-], " +
                    "found `${current.character}` at ${current.index}"

            is ErrorKind.ParseSimpleLength ->
                "invalid length: expected length 32 for simple format, found ${current.len}"

            is ErrorKind.ParseByteLength ->
                "invalid length: expected 16 bytes, found ${current.len}"

            is ErrorKind.ParseGroupCount ->
                "invalid group count: expected 5, found ${current.count}"

            is ErrorKind.ParseGroupLength -> {
                val expected = intArrayOf(8, 4, 4, 4, 12)[current.group]
                "invalid group length in group ${current.group}: expected $expected, found ${current.len}"
            }

            ErrorKind.ParseInvalidUtf8 -> "non-UTF8 input"
            ErrorKind.Nil -> "the UUID is nil"
            ErrorKind.ParseOther -> "failed to parse a UUID"
            is ErrorKind.InvalidSystemTime -> "the system timestamp is invalid: ${current.reason}"
        }
}

internal sealed interface ErrorKind {
    /**
     * Invalid character in the UUID string.
     */
    data class ParseChar(
        val character: Char,
        val index: Int,
    ) : ErrorKind

    /**
     * A simple UUID did not contain 32 characters.
     */
    data class ParseSimpleLength(
        val len: Int,
    ) : ErrorKind

    /**
     * A byte array did not contain 16 bytes.
     */
    data class ParseByteLength(
        val len: Int,
    ) : ErrorKind

    /**
     * A hyphenated UUID did not contain 5 groups.
     */
    data class ParseGroupCount(
        val count: Int,
    ) : ErrorKind

    /**
     * A hyphenated UUID had a group that was not the right length.
     */
    data class ParseGroupLength(
        val group: Int,
        val len: Int,
        val index: Int,
    ) : ErrorKind

    /**
     * The input was not a valid UTF-8 string.
     */
    data object ParseInvalidUtf8 : ErrorKind

    /**
     * Some other parsing error occurred.
     */
    data object ParseOther : ErrorKind

    /**
     * The UUID is nil.
     */
    data object Nil : ErrorKind

    /**
     * A system time was invalid.
     */
    data class InvalidSystemTime(
        val reason: String,
    ) : ErrorKind
}

/**
 * A string that is guaranteed to fail to parse to a [Uuid].
 *
 * This type acts as a lightweight error indicator, suggesting that the string
 * cannot be parsed but offering no error details. To get details, use
 * [InvalidUuid.intoErr].
 */
@HiddenFromObjC
public class InvalidUuid internal constructor(
    private val bytes: ByteArray,
) : Exception() {
    /**
     * Converts the lightweight error type into detailed diagnostics.
     */
    public fun intoErr(): Error {
        val inputString =
            try {
                bytes.decodeToString(throwOnInvalidSequence = true)
            } catch (_: Throwable) {
                return Error(ErrorKind.ParseInvalidUtf8)
            }

        val (uuidString, offset, simple) =
            when {
                inputString.length >= 2 && inputString.first() == '{' && inputString.last() == '}' ->
                    Triple(inputString.substring(1, inputString.length - 1), 1, false)

                inputString.startsWith("urn:uuid:") ->
                    Triple(inputString.substring("urn:uuid:".length), "urn:uuid:".length, false)

                else -> Triple(inputString, 0, true)
            }

        var hyphenCount = 0
        val groupBounds = IntArray(4)
        var byteIndex = 0

        for (character in uuidString) {
            val byteWidth = character.toString().encodeToByteArray().size
            val byte = character.code
            if (byte > ASCII_MAX) {
                return Error(
                    ErrorKind.ParseChar(
                        character = character,
                        index = byteIndex + offset + 1,
                    ),
                )
            } else if (byte == HYPHEN_CODE) {
                if (hyphenCount < 4) {
                    groupBounds[hyphenCount] = byteIndex
                }
                hyphenCount += 1
            } else if (!character.isAsciiHexDigit()) {
                return Error(
                    ErrorKind.ParseChar(
                        character = character,
                        index = byteIndex + offset + 1,
                    ),
                )
            }
            byteIndex += byteWidth
        }

        return if (hyphenCount == 0 && simple) {
            Error(ErrorKind.ParseSimpleLength(len = inputString.length))
        } else if (hyphenCount != 4) {
            Error(ErrorKind.ParseGroupCount(count = hyphenCount + 1))
        } else {
            val blockStarts = intArrayOf(0, 9, 14, 19, 24)
            for (i in 0 until 4) {
                if (groupBounds[i] != blockStarts[i + 1] - 1) {
                    return Error(
                        ErrorKind.ParseGroupLength(
                            group = i,
                            len = groupBounds[i] - blockStarts[i],
                            index = offset + blockStarts[i] + 1,
                        ),
                    )
                }
            }

            Error(
                ErrorKind.ParseGroupLength(
                    group = 4,
                    len = inputString.length - blockStarts[4],
                    index = offset + blockStarts[4] + 1,
                ),
            )
        }
    }
}

private const val ASCII_MAX: Int = 0x7f
private const val HYPHEN_CODE: Int = 45

private fun Char.isAsciiHexDigit(): Boolean =
    this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
