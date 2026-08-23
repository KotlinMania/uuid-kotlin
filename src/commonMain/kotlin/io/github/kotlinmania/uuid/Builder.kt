// port-lint: source builder.rs
package io.github.kotlinmania.uuid

/**
 * A builder for creating a [Uuid].
 */
public class Builder(
    private var uuid: Uuid,
) {
    /**
     * Specifies the variant of the UUID.
     */
    public fun setVariant(v: Variant): Builder {
        this.uuid = withVariant(v).intoUuid()
        return this
    }

    /**
     * Specifies the variant of the UUID, returning a new Builder.
     */
    public fun withVariant(v: Variant): Builder {
        val bytes = uuid.asBytes()
        val byte = bytes[8].toInt() and 0xFF
        bytes[8] =
            when (v) {
                Variant.NCS -> (byte and 0x7F).toByte()
                Variant.RFC4122 -> ((byte and 0x3F) or 0x80).toByte()
                Variant.Microsoft -> ((byte and 0x1F) or 0xC0).toByte()
                Variant.Future -> (byte or 0xE0).toByte()
            }
        return Builder(Uuid.fromBytes(bytes))
    }

    /**
     * Specifies the version number of the UUID.
     */
    public fun setVersion(v: Version): Builder {
        this.uuid = withVersion(v).intoUuid()
        return this
    }

    /**
     * Specifies the version number of the UUID, returning a new Builder.
     */
    public fun withVersion(v: Version): Builder {
        val bytes = uuid.asBytes()
        val verNum = if (v == Version.Max) 0x0F else (v.number and 0x0F)
        bytes[6] = (((bytes[6].toInt() and 0x0F) or (verNum shl 4))).toByte()
        return Builder(Uuid.fromBytes(bytes))
    }

    /**
     * Get a reference to the underlying [Uuid].
     */
    public fun asUuid(): Uuid = uuid

    /**
     * Convert the builder into a [Uuid].
     */
    public fun intoUuid(): Uuid = uuid

    override fun equals(other: Any?): Boolean =
        other is Builder && uuid == other.uuid

    override fun hashCode(): Int = uuid.hashCode()

    override fun toString(): String = "Builder($uuid)"

    public companion object {
        /**
         * Creates a `Builder` using the supplied bytes.
         */
        public fun fromBytes(b: ByteArray): Builder = Builder(Uuid.fromBytes(b))

        /**
         * Creates a `Builder` using the supplied bytes in little endian order.
         */
        public fun fromBytesLe(b: ByteArray): Builder = Builder(Uuid.fromBytesLe(b))

        /**
         * Creates a `Builder` for a version 1 UUID using the supplied timestamp, counter, and node ID.
         */
        public fun fromGregorianTimestamp(ticks: ULong, counter: UShort, nodeId: ByteArray): Builder =
            Builder(encodeGregorianTimestamp(ticks, counter, nodeId))

        /**
         * Creates a `Builder` for a version 3 UUID using the supplied MD5 hashed bytes.
         */
        public fun fromMd5Bytes(md5Bytes: ByteArray): Builder =
            Builder(Uuid.fromBytes(md5Bytes))
                .withVariant(Variant.RFC4122)
                .withVersion(Version.Md5)

        /**
         * Creates a `Builder` for a version 4 UUID using the supplied random bytes.
         */
        public fun fromRandomBytes(randomBytes: ByteArray): Builder =
            Builder(Uuid.fromBytes(randomBytes))
                .withVariant(Variant.RFC4122)
                .withVersion(Version.Random)

        /**
         * Creates a `Builder` for a version 5 UUID using the supplied SHA-1 hashed bytes.
         */
        public fun fromSha1Bytes(sha1Bytes: ByteArray): Builder =
            Builder(Uuid.fromBytes(sha1Bytes))
                .withVariant(Variant.RFC4122)
                .withVersion(Version.Sha1)

        /**
         * Creates a `Builder` for a version 6 UUID using the supplied timestamp, counter, and node ID.
         */
        public fun fromSortedGregorianTimestamp(ticks: ULong, counter: UShort, nodeId: ByteArray): Builder =
            Builder(encodeSortedGregorianTimestamp(ticks, counter, nodeId))

        /**
         * Creates a `Builder` for a version 7 UUID using the supplied Unix timestamp and counter bytes.
         */
        public fun fromUnixTimestampMillis(millis: ULong, counterRandomBytes: ByteArray): Builder =
            Builder(encodeUnixTimestampMillis(millis, counterRandomBytes))

        /**
         * Creates a `Builder` for a version 8 UUID using the supplied user-defined bytes.
         */
        public fun fromCustomBytes(customBytes: ByteArray): Builder =
            Builder(Uuid.fromBytes(customBytes))
                .withVariant(Variant.RFC4122)
                .withVersion(Version.Custom)

        /**
         * Creates a `Builder` using the supplied byte slice.
         */
        public fun fromSlice(b: ByteArray): Result<Builder> =
            Uuid.fromSlice(b).map { Builder(it) }

        /**
         * Creates a `Builder` using the supplied bytes in little endian order.
         */
        public fun fromSliceLe(b: ByteArray): Result<Builder> =
            if (b.size == 16) {
                Result.success(fromBytesLe(b))
            } else {
                Result.failure(Error(ErrorKind.ParseByteLength(b.size)))
            }

        /**
         * Creates a `Builder` from four field values.
         */
        public fun fromFields(d1: UInt, d2: UShort, d3: UShort, d4: ByteArray): Builder =
            Builder(Uuid.fromFields(d1, d2, d3, d4))

        /**
         * Creates a `Builder` from four field values in little-endian order.
         */
        public fun fromFieldsLe(d1: UInt, d2: UShort, d3: UShort, d4: ByteArray): Builder =
            Builder(Uuid.fromFieldsLe(d1, d2, d3, d4))

        /**
         * Creates a `Builder` with an initial `Uuid.nil()`.
         */
        public fun nil(): Builder = Builder(Uuid.nil())
    }
}
