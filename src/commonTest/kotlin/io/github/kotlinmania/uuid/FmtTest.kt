// port-lint: tests fmt.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FmtTest {
    @Test
    fun uuidDisplayUsesHyphenatedLowercase() {
        val uuid = fixture()

        assertEquals("f9168c5e-ceb2-4faa-b6bf-329bf39fa1e4", uuid.toString())
        assertEquals(uuid.toString(), uuid.hyphenated().toString())
    }

    @Test
    fun formatAdaptersEncodeLowerAndUpper() {
        val uuid = Uuid.parseStr("936DA01f9abd4d9d80c702af85c822a8")

        assertEquals("936da01f-9abd-4d9d-80c7-02af85c822a8", uuid.hyphenated().encodeLower(Uuid.encodeBuffer()))
        assertEquals("936DA01F-9ABD-4D9D-80C7-02AF85C822A8", uuid.hyphenated().encodeUpper(Uuid.encodeBuffer()))
        assertEquals("936da01f9abd4d9d80c702af85c822a8", uuid.simple().encodeLower(Uuid.encodeBuffer()))
        assertEquals("936DA01F9ABD4D9D80C702AF85C822A8", uuid.simple().encodeUpper(Uuid.encodeBuffer()))
        assertEquals("{936da01f-9abd-4d9d-80c7-02af85c822a8}", uuid.braced().encodeLower(Uuid.encodeBuffer()))
        assertEquals("{936DA01F-9ABD-4D9D-80C7-02AF85C822A8}", uuid.braced().encodeUpper(Uuid.encodeBuffer()))
        assertEquals("urn:uuid:936da01f-9abd-4d9d-80c7-02af85c822a8", uuid.urn().encodeLower(Uuid.encodeBuffer()))
        assertEquals("urn:uuid:936DA01F-9ABD-4D9D-80C7-02AF85C822A8", uuid.urn().encodeUpper(Uuid.encodeBuffer()))
    }

    @Test
    fun trailingBufferContentsArePreserved() {
        val uuid = Uuid.nil()
        val buffer = ByteArray(100) { 'x'.code.toByte() }

        val encoded = uuid.hyphenated().encodeLower(buffer)

        assertEquals(Hyphenated.LENGTH, encoded.length)
        assertTrue(buffer.drop(Hyphenated.LENGTH).all { it == 'x'.code.toByte() })
    }

    @Test
    fun eachAdapterPreservesTrailingContents() {
        val uuid = Uuid.nil()

        assertTrailing(Simple.LENGTH) { buffer -> uuid.simple().encodeLower(buffer) }
        assertTrailing(Hyphenated.LENGTH) { buffer -> uuid.asHyphenated().encodeLower(buffer) }
        assertTrailing(Urn.LENGTH) { buffer -> uuid.urn().encodeLower(buffer) }
        assertTrailing(Braced.LENGTH) { buffer -> uuid.asBraced().encodeLower(buffer) }
    }

    @Test
    fun adaptersRejectTooSmallBuffers() {
        val uuid = Uuid.nil()

        assertFailsWith<IllegalArgumentException> { uuid.hyphenated().encodeLower(ByteArray(Hyphenated.LENGTH - 1)) }
        assertFailsWith<IllegalArgumentException> { uuid.simple().encodeLower(ByteArray(Simple.LENGTH - 1)) }
        assertFailsWith<IllegalArgumentException> { uuid.urn().encodeLower(ByteArray(Urn.LENGTH - 1)) }
        assertFailsWith<IllegalArgumentException> { uuid.braced().encodeLower(ByteArray(Braced.LENGTH - 1)) }
    }

    @Test
    fun adaptersConvertBackToUuid() {
        val uuid = Uuid.nil()

        assertEquals(uuid, uuid.hyphenated().intoUuid())
        assertEquals(uuid, uuid.simple().intoUuid())
        assertEquals(uuid, uuid.urn().intoUuid())
        assertEquals(uuid, uuid.braced().intoUuid())
    }

    @Test
    fun adapterParsingUsesTheMatchingShape() {
        val uuid = fixture()

        assertEquals(uuid, Hyphenated.parse("f9168c5e-ceb2-4faa-b6bf-329bf39fa1e4").getOrThrow().asUuid())
        assertEquals(uuid, Simple.parse("f9168c5eceb24faab6bf329bf39fa1e4").getOrThrow().asUuid())
        assertEquals(uuid, Urn.parse("urn:uuid:f9168c5e-ceb2-4faa-b6bf-329bf39fa1e4").getOrThrow().asUuid())
        assertEquals(uuid, Braced.parse("{f9168c5e-ceb2-4faa-b6bf-329bf39fa1e4}").getOrThrow().asUuid())
    }

    private fun assertTrailing(
        expectedLength: Int,
        encode: (ByteArray) -> String,
    ) {
        val buffer = ByteArray(100) { 'x'.code.toByte() }
        val original = buffer.copyOf()
        val encoded = encode(buffer)

        assertEquals(expectedLength, encoded.length)
        assertContentEquals(original.copyOfRange(expectedLength, original.size), buffer.copyOfRange(expectedLength, buffer.size))
    }
}
