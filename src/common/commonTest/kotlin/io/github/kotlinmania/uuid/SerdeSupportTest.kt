// port-lint: tests external/serde_support.rs
package io.github.kotlinmania.uuid

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.MapAccess
import io.github.kotlinmania.serdecore.de.SeqAccess
import io.github.kotlinmania.serdecore.de.StringDeserialize
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.serdetest.Token
import io.github.kotlinmania.serdetest.assertDeTokens
import io.github.kotlinmania.serdetest.assertDeTokensError
import io.github.kotlinmania.serdetest.assertSerTokens
import io.github.kotlinmania.serdetest.compact
import io.github.kotlinmania.serdetest.readable
import io.github.kotlinmania.uuid.external.SerdeSupport
import kotlin.test.Test

class SerdeSupportTest {
    private data class UuidContainer(
        val u: Uuid,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
            val s =
                serializer.serializeStruct("UuidContainer", 1).getOrNull()
                    ?: return SerdeResult.failure(SerdeError.custom("failed to serialize struct"))
            val fieldRes =
                s.serializeField(
                    "u",
                    object : Serialize {
                        override fun <Ok2> serialize(serializer: Serializer<Ok2>): SerdeResult<Ok2> =
                            SerdeSupport.Compact.serialize(u, serializer)
                    },
                )
            if (fieldRes.isFailure) return SerdeResult.failure(fieldRes.exceptionOrNull()!!)
            return s.end()
        }

        companion object : Deserialize<UuidContainer> {
            override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<UuidContainer> =
                deserializer.deserializeStruct(
                    "UuidContainer",
                    listOf("u"),
                    object : Visitor<UuidContainer> {
                        override fun expecting(): String = "struct UuidContainer"

                        override fun <A : MapAccess> visitMap(access: A): SerdeResult<UuidContainer> {
                            var u: Uuid? = null
                            while (true) {
                                val key = access.nextKey(StringDeserialize).getOrNull() ?: break
                                if (key == "u") {
                                    u =
                                        access
                                            .nextValue(
                                                object : Deserialize<Uuid> {
                                                    override fun <D2 : Deserializer> deserialize(deserializer: D2): SerdeResult<Uuid> =
                                                        SerdeSupport.Compact.deserialize(deserializer)
                                                },
                                            ).getOrNull()
                                }
                            }
                            val uuid = u ?: return SerdeResult.failure(SerdeError.missingField("u"))
                            return SerdeResult.success(UuidContainer(uuid))
                        }
                    },
                )
        }
    }

    private data class SimpleStruct(
        val id: Uuid,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
            serializer.serializeNewtypeStruct(
                "Struct",
                object : Serialize {
                    override fun <Ok2> serialize(serializer: Serializer<Ok2>): SerdeResult<Ok2> =
                        SerdeSupport.SimpleFormat.serialize(id, serializer)
                },
            )

        companion object : Deserialize<SimpleStruct> {
            override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<SimpleStruct> =
                deserializer.deserializeTupleStruct(
                    "Struct",
                    1,
                    object : Visitor<SimpleStruct> {
                        override fun expecting(): String = "tuple struct Struct"

                        override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<SimpleStruct> {
                            val next =
                                access.nextElement(
                                    object : Deserialize<Uuid> {
                                        override fun <D2 : Deserializer> deserialize(deserializer: D2): SerdeResult<Uuid> =
                                            SerdeSupport.SimpleFormat.deserialize(deserializer)
                                    },
                                )
                            if (next.isFailure) return SerdeResult.failure(next.exceptionOrNull()!!)
                            val uuid = next.getOrNull() ?: return SerdeResult.failure(SerdeError.invalidLength(0, this))
                            return SerdeResult.success(SimpleStruct(uuid))
                        }
                    },
                )
        }
    }

    private data class BracedStruct(
        val id: Uuid,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
            serializer.serializeNewtypeStruct(
                "Struct",
                object : Serialize {
                    override fun <Ok2> serialize(serializer: Serializer<Ok2>): SerdeResult<Ok2> =
                        SerdeSupport.BracedFormat.serialize(id, serializer)
                },
            )

        companion object : Deserialize<BracedStruct> {
            override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<BracedStruct> =
                deserializer.deserializeTupleStruct(
                    "Struct",
                    1,
                    object : Visitor<BracedStruct> {
                        override fun expecting(): String = "tuple struct Struct"

                        override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<BracedStruct> {
                            val next =
                                access.nextElement(
                                    object : Deserialize<Uuid> {
                                        override fun <D2 : Deserializer> deserialize(deserializer: D2): SerdeResult<Uuid> =
                                            SerdeSupport.BracedFormat.deserialize(deserializer)
                                    },
                                )
                            if (next.isFailure) return SerdeResult.failure(next.exceptionOrNull()!!)
                            val uuid = next.getOrNull() ?: return SerdeResult.failure(SerdeError.invalidLength(0, this))
                            return SerdeResult.success(BracedStruct(uuid))
                        }
                    },
                )
        }
    }

    private data class UrnStruct(
        val id: Uuid,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
            serializer.serializeNewtypeStruct(
                "Struct",
                object : Serialize {
                    override fun <Ok2> serialize(serializer: Serializer<Ok2>): SerdeResult<Ok2> =
                        SerdeSupport.UrnFormat.serialize(id, serializer)
                },
            )

        companion object : Deserialize<UrnStruct> {
            override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<UrnStruct> =
                deserializer.deserializeTupleStruct(
                    "Struct",
                    1,
                    object : Visitor<UrnStruct> {
                        override fun expecting(): String = "tuple struct Struct"

                        override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<UrnStruct> {
                            val next =
                                access.nextElement(
                                    object : Deserialize<Uuid> {
                                        override fun <D2 : Deserializer> deserialize(deserializer: D2): SerdeResult<Uuid> =
                                            SerdeSupport.UrnFormat.deserialize(deserializer)
                                    },
                                )
                            if (next.isFailure) return SerdeResult.failure(next.exceptionOrNull()!!)
                            val uuid = next.getOrNull() ?: return SerdeResult.failure(SerdeError.invalidLength(0, this))
                            return SerdeResult.success(UrnStruct(uuid))
                        }
                    },
                )
        }
    }

    @Test
    fun testSerializeCompact() {
        val uuidBytes = "F9168C5E-CEB2-4F".encodeToByteArray()
        val container = UuidContainer(Uuid.fromSlice(uuidBytes).getOrThrow())
        val tokens =
            mutableListOf<Token>(
                Token.Struct("UuidContainer", 1),
                Token.Str("u"),
                Token.Tuple(16),
            )
        for (b in uuidBytes) {
            tokens.add(Token.U8(b.toUByte()))
        }
        tokens.add(Token.TupleEnd)
        tokens.add(Token.StructEnd)

        assertSerTokens(container.compact(), tokens)
        assertDeTokens(container, UuidContainer.compact(), tokens)
    }

    @Test
    fun testSerializeAsSimple() {
        val u = SimpleStruct(Uuid.parseStr(HYPHENATED_UUID_STR))
        assertSerTokens(
            u,
            listOf(
                Token.NewtypeStruct("Struct"),
                Token.Str(SIMPLE_UUID_STR),
            ),
        )
    }

    @Test
    fun testDeFromSimple() {
        val s = SimpleStruct(Uuid.parseStr(HYPHENATED_UUID_STR))
        assertDeTokens(
            s,
            SimpleStruct,
            listOf(
                Token.TupleStruct("Struct", 1),
                Token.BorrowedStr(SIMPLE_UUID_STR),
                Token.TupleStructEnd,
            ),
        )
    }

    @Test
    fun testDeRejectHyphenatedSimple() {
        assertDeTokensError(
            SimpleStruct.readable(),
            listOf(
                Token.TupleStruct("Struct", 1),
                Token.BorrowedStr(HYPHENATED_UUID_STR),
                Token.TupleStructEnd,
            ),
            "UUID parsing failed: invalid group length in group 4: expected 12, found 12",
        )
    }

    @Test
    fun testSerializeAsBraced() {
        val u = BracedStruct(Uuid.parseStr(HYPHENATED_UUID_STR))
        assertSerTokens(
            u,
            listOf(
                Token.NewtypeStruct("Struct"),
                Token.Str(BRACED_UUID_STR),
            ),
        )
    }

    @Test
    fun testDeFromBraced() {
        val s = BracedStruct(Uuid.parseStr(HYPHENATED_UUID_STR))
        assertDeTokens(
            s,
            BracedStruct,
            listOf(
                Token.TupleStruct("Struct", 1),
                Token.BorrowedStr(BRACED_UUID_STR),
                Token.TupleStructEnd,
            ),
        )
    }

    @Test
    fun testDeRejectHyphenatedBraced() {
        assertDeTokensError(
            BracedStruct.readable(),
            listOf(
                Token.TupleStruct("Struct", 1),
                Token.BorrowedStr(HYPHENATED_UUID_STR),
                Token.TupleStructEnd,
            ),
            "UUID parsing failed: invalid group length in group 4: expected 12, found 12",
        )
    }

    @Test
    fun testSerializeAsUrn() {
        val u = UrnStruct(Uuid.parseStr(HYPHENATED_UUID_STR))
        assertSerTokens(
            u,
            listOf(
                Token.NewtypeStruct("Struct"),
                Token.Str(URN_UUID_STR),
            ),
        )
    }

    @Test
    fun testDeFromUrn() {
        val s = UrnStruct(Uuid.parseStr(HYPHENATED_UUID_STR))
        assertDeTokens(
            s,
            UrnStruct,
            listOf(
                Token.TupleStruct("Struct", 1),
                Token.BorrowedStr(URN_UUID_STR),
                Token.TupleStructEnd,
            ),
        )
    }

    @Test
    fun testDeRejectHyphenatedUrn() {
        assertDeTokensError(
            UrnStruct.readable(),
            listOf(
                Token.TupleStruct("Struct", 1),
                Token.BorrowedStr(HYPHENATED_UUID_STR),
                Token.TupleStructEnd,
            ),
            "UUID parsing failed: invalid group length in group 4: expected 12, found 12",
        )
    }

    @Test
    fun testSerializeReadableString() {
        val uuidStr = "f9168c5e-ceb2-4faa-b6bf-329bf39fa1e4"
        val u = Uuid.parseStr(uuidStr)
        assertSerTokens(u.readable(), listOf(Token.Str(uuidStr)))
        assertDeTokens(u, Uuid.readable(), listOf(Token.Str(uuidStr)))
    }

    @Test
    fun testDeserializeReadableCompact() {
        val uuidBytes = "F9168C5E-CEB2-4F".encodeToByteArray()
        val u = Uuid.fromSlice(uuidBytes).getOrThrow()
        val tokens = mutableListOf<Token>(Token.Tuple(16))
        for (b in uuidBytes) {
            tokens.add(Token.U8(b.toUByte()))
        }
        tokens.add(Token.TupleEnd)
        assertDeTokens(u, Uuid.readable(), tokens)
    }

    @Test
    fun testDeserializeReadableBytes() {
        val uuidBytes = "F9168C5E-CEB2-4F".encodeToByteArray()
        val u = Uuid.fromSlice(uuidBytes).getOrThrow()
        assertDeTokens(u, Uuid.readable(), listOf(Token.Bytes(uuidBytes)))
    }

    @Test
    fun testSerializeHyphenated() {
        val uuidStr = "f9168c5e-ceb2-4faa-b6bf-329bf39fa1e4"
        val u = Uuid.parseStr(uuidStr)
        assertSerTokens(u.hyphenated(), listOf(Token.Str(uuidStr)))
        assertDeTokens(u.hyphenated(), Hyphenated, listOf(Token.Str(uuidStr)))
    }

    @Test
    fun testSerializeSimple() {
        val uuidStr = "f9168c5eceb24faab6bf329bf39fa1e4"
        val u = Uuid.parseStr(uuidStr)
        assertSerTokens(u.simple(), listOf(Token.Str(uuidStr)))
        assertDeTokens(u.simple(), Simple, listOf(Token.Str(uuidStr)))
    }

    @Test
    fun testSerializeUrn() {
        val uuidStr = "urn:uuid:f9168c5e-ceb2-4faa-b6bf-329bf39fa1e4"
        val u = Uuid.parseStr(uuidStr)
        assertSerTokens(u.urn(), listOf(Token.Str(uuidStr)))
        assertDeTokens(u.urn(), Urn, listOf(Token.Str(uuidStr)))
    }

    @Test
    fun testSerializeBraced() {
        val uuidStr = "{f9168c5e-ceb2-4faa-b6bf-329bf39fa1e4}"
        val u = Uuid.parseStr(uuidStr)
        assertSerTokens(u.braced(), listOf(Token.Str(uuidStr)))
        assertDeTokens(u.braced(), Braced, listOf(Token.Str(uuidStr)))
    }

    @Test
    fun testSerializeNonHumanReadable() {
        val uuidBytes = "F9168C5E-CEB2-4F".encodeToByteArray()
        val u = Uuid.fromSlice(uuidBytes).getOrThrow()
        val expectedBytes =
            byteArrayOf(
                70,
                57,
                49,
                54,
                56,
                67,
                53,
                69,
                45,
                67,
                69,
                66,
                50,
                45,
                52,
                70,
            )
        assertSerTokens(u.compact(), listOf(Token.Bytes(expectedBytes)))
        assertDeTokens(u, Uuid.compact(), listOf(Token.Bytes(expectedBytes)))
    }

    @Test
    fun testDeFailure() {
        assertDeTokensError(
            Uuid.readable(),
            listOf(Token.Str("hello_world")),
            "UUID parsing failed: invalid character: expected an optional prefix of `urn:uuid:` followed by [0-9a-fA-F-], found `h` at 1",
        )

        assertDeTokensError(
            Uuid.compact(),
            listOf(Token.Bytes("hello_world".encodeToByteArray())),
            "UUID parsing failed: invalid length: expected 16 bytes, found 11",
        )
    }

    @Test
    fun testSerdeNonNilUuid() {
        val uuidStr = "f9168c5e-ceb2-4faa-b6bf-329bf39fa1e4"
        val uuid = Uuid.parseStr(uuidStr)
        val nonNilUuid = NonNilUuid.tryFrom(uuid).getOrThrow()

        assertSerTokens(nonNilUuid.readable(), listOf(Token.Str(uuidStr)))
        assertDeTokens(nonNilUuid, NonNilUuid.readable(), listOf(Token.Str(uuidStr)))
    }

    companion object {
        private const val HYPHENATED_UUID_STR = "f9168c5e-ceb2-4faa-b6bf-329bf39fa1e4"
        private const val SIMPLE_UUID_STR = "f9168c5eceb24faab6bf329bf39fa1e4"
        private const val BRACED_UUID_STR = "{f9168c5e-ceb2-4faa-b6bf-329bf39fa1e4}"
        private const val URN_UUID_STR = "urn:uuid:f9168c5e-ceb2-4faa-b6bf-329bf39fa1e4"
    }
}
