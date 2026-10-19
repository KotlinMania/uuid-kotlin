// port-lint: source external/serde_support.rs
package io.github.kotlinmania.uuid.external

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.SeqAccess
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.uuid.Braced
import io.github.kotlinmania.uuid.Simple
import io.github.kotlinmania.uuid.Urn
import io.github.kotlinmania.uuid.Uuid
import io.github.kotlinmania.uuid.braced
import io.github.kotlinmania.uuid.simple
import io.github.kotlinmania.uuid.urn

/**
 * Trait for constructing UUID formats during deserialization.
 */
internal interface UuidDeserialize<T : Any> {
    fun fromStr(formatted: String): Result<T>

    fun fromSlice(bytes: ByteArray): Result<T>

    fun fromBytes(bytes: ByteArray): Result<T>
}

/**
 * Visitor for human-readable UUID formats.
 */
internal class UuidReadableVisitor<T : Any>(
    private val expectingMessage: String,
    private val fromStr: (String) -> Result<T>,
    private val fromSlice: (ByteArray) -> Result<T>,
    private val fromBytes: (ByteArray) -> Result<T>,
) : Visitor<T> {
    override fun expecting(): String = expectingMessage

    override fun visitStr(v: String): SerdeResult<T> =
        fromStr(v).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { deError(it) },
        )

    override fun visitBorrowedStr(v: String): SerdeResult<T> = visitStr(v)

    override fun visitString(v: String): SerdeResult<T> = visitStr(v)

    override fun visitBytes(v: ByteArray): SerdeResult<T> =
        fromSlice(v).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { deError(it) },
        )

    override fun visitBorrowedBytes(v: ByteArray): SerdeResult<T> = visitBytes(v)

    override fun visitByteBuf(v: ByteArray): SerdeResult<T> = visitBytes(v)

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<T> {
        val bytes = ByteArray(16)
        for (i in 0 until 16) {
            val elem =
                access.nextElement(UuidByteElementDeserialize).getOrNull()
                    ?: return SerdeResult.failure(SerdeError.invalidLength(i, this))
            bytes[i] = elem
        }
        return fromBytes(bytes).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { deError(it) },
        )
    }
}

/**
 * Visitor for raw byte representation of a UUID.
 */
internal class UuidBytesVisitor<T : Any>(
    private val fromSlice: (ByteArray) -> Result<T>,
) : Visitor<T> {
    override fun expecting(): String = "a 16 byte array"

    override fun visitBytes(v: ByteArray): SerdeResult<T> =
        fromSlice(v).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { deError(it) },
        )

    override fun visitBorrowedBytes(v: ByteArray): SerdeResult<T> = visitBytes(v)

    override fun visitByteBuf(v: ByteArray): SerdeResult<T> = visitBytes(v)
}

internal class UuidByteElementSerialize(
    private val value: Byte,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeU8(value.toUByte())
}

internal object UuidByteElementDeserialize : Deserialize<Byte> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Byte> =
        deserializer.deserializeAny(
            object : Visitor<Byte> {
                override fun expecting(): String = "a byte"

                override fun visitU8(v: UByte): SerdeResult<Byte> = SerdeResult.success(v.toByte())

                override fun visitI8(v: Byte): SerdeResult<Byte> = SerdeResult.success(v)

                override fun visitU16(v: UShort): SerdeResult<Byte> = SerdeResult.success(v.toByte())

                override fun visitI16(v: Short): SerdeResult<Byte> = SerdeResult.success(v.toByte())

                override fun visitU32(v: UInt): SerdeResult<Byte> = SerdeResult.success(v.toByte())

                override fun visitI32(v: Int): SerdeResult<Byte> = SerdeResult.success(v.toByte())

                override fun visitU64(v: ULong): SerdeResult<Byte> = SerdeResult.success(v.toByte())

                override fun visitI64(v: Long): SerdeResult<Byte> = SerdeResult.success(v.toByte())
            },
        )
}

internal fun <T> deError(e: Throwable): SerdeResult<T> =
    SerdeResult.failure(SerdeError.custom("UUID parsing failed: ${e.message}"))

/**
 * Serialization and deserialization adapters for [Uuid].
 */
public object SerdeSupport {
    /**
     * Serializes a [Uuid] as a `[u8; 16]`.
     */
    public object Compact {
        public fun <Ok> serialize(u: Uuid, serializer: Serializer<Ok>): SerdeResult<Ok> {
            val tuple =
                serializer.serializeTuple(16).getOrNull()
                    ?: return SerdeResult.failure(SerdeError.custom("failed to serialize tuple"))
            for (b in u.asBytes()) {
                val elemRes = tuple.serializeElement(UuidByteElementSerialize(b))
                if (elemRes.isFailure) return SerdeResult.failure(elemRes.exceptionOrNull()!!)
            }
            return tuple.end()
        }

        public fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Uuid> =
            deserializer.deserializeTuple(
                16,
                object : Visitor<Uuid> {
                    override fun expecting(): String = "a 16 byte array"

                    override fun visitBytes(v: ByteArray): SerdeResult<Uuid> =
                        if (v.size == 16) {
                            SerdeResult.success(Uuid.fromBytes(v))
                        } else {
                            SerdeResult.failure(SerdeError.invalidLength(v.size, this))
                        }

                    override fun visitBorrowedBytes(v: ByteArray): SerdeResult<Uuid> = visitBytes(v)

                    override fun visitByteBuf(v: ByteArray): SerdeResult<Uuid> = visitBytes(v)

                    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<Uuid> {
                        val bytes = ByteArray(16)
                        for (i in 0 until 16) {
                            val next = access.nextElement(UuidByteElementDeserialize)
                            if (next.isFailure) return SerdeResult.failure(next.exceptionOrNull()!!)
                            val elem =
                                next.getOrNull()
                                    ?: return SerdeResult.failure(SerdeError.invalidLength(i, this))
                            bytes[i] = elem
                        }
                        return SerdeResult.success(Uuid.fromBytes(bytes))
                    }
                },
            )
    }

    /**
     * Serializes a [Uuid] as a simple formatted string.
     */
    public object SimpleFormat {
        public fun <Ok> serialize(u: Uuid, serializer: Serializer<Ok>): SerdeResult<Ok> =
            serializer.serializeStr(u.simple().toString())

        public fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Uuid> =
            Simple.deserialize(deserializer).map { it.intoUuid() }
    }

    /**
     * Serializes a [Uuid] as a braced formatted string.
     */
    public object BracedFormat {
        public fun <Ok> serialize(u: Uuid, serializer: Serializer<Ok>): SerdeResult<Ok> =
            serializer.serializeStr(u.braced().toString())

        public fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Uuid> =
            Braced.deserialize(deserializer).map { it.intoUuid() }
    }

    /**
     * Serializes a [Uuid] as a URN formatted string.
     */
    public object UrnFormat {
        public fun <Ok> serialize(u: Uuid, serializer: Serializer<Ok>): SerdeResult<Ok> =
            serializer.serializeStr(u.urn().toString())

        public fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Uuid> =
            Urn.deserialize(deserializer).map { it.intoUuid() }
    }
}
