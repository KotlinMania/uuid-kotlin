// port-lint: tests uuid/src/fmt.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FmtTest {
    @Test
    fun hyphenatedTrailing() {
        val buf = ByteArray(100) { 'x'.code.toByte() }
        val len = Uuid.nil().hyphenated().encodeLower(buf).length
        assertEquals(Hyphenated.LENGTH, len)
        assertTrue(buf.drop(len).all { it == 'x'.code.toByte() })
    }

    @Test
    fun hyphenatedRefTrailing() {
        val buf = ByteArray(100) { 'x'.code.toByte() }
        val len = Uuid.nil().asHyphenated().encodeLower(buf).length
        assertEquals(Hyphenated.LENGTH, len)
        assertTrue(buf.drop(len).all { it == 'x'.code.toByte() })
    }

    @Test
    fun simpleTrailing() {
        val buf = ByteArray(100) { 'x'.code.toByte() }
        val len = Uuid.nil().simple().encodeLower(buf).length
        assertEquals(Simple.LENGTH, len)
        assertTrue(buf.drop(len).all { it == 'x'.code.toByte() })
    }

    @Test
    fun simpleRefTrailing() {
        val buf = ByteArray(100) { 'x'.code.toByte() }
        val len = Uuid.nil().asSimple().encodeLower(buf).length
        assertEquals(Simple.LENGTH, len)
        assertTrue(buf.drop(len).all { it == 'x'.code.toByte() })
    }

    @Test
    fun urnTrailing() {
        val buf = ByteArray(100) { 'x'.code.toByte() }
        val len = Uuid.nil().urn().encodeLower(buf).length
        assertEquals(Urn.LENGTH, len)
        assertTrue(buf.drop(len).all { it == 'x'.code.toByte() })
    }

    @Test
    fun urnRefTrailing() {
        val buf = ByteArray(100) { 'x'.code.toByte() }
        val len = Uuid.nil().asUrn().encodeLower(buf).length
        assertEquals(Urn.LENGTH, len)
        assertTrue(buf.drop(len).all { it == 'x'.code.toByte() })
    }

    @Test
    fun bracedTrailing() {
        val buf = ByteArray(100) { 'x'.code.toByte() }
        val len = Uuid.nil().braced().encodeLower(buf).length
        assertEquals(Braced.LENGTH, len)
        assertTrue(buf.drop(len).all { it == 'x'.code.toByte() })
    }

    @Test
    fun bracedRefTrailing() {
        val buf = ByteArray(100) { 'x'.code.toByte() }
        val len = Uuid.nil().asBraced().encodeLower(buf).length
        assertEquals(Braced.LENGTH, len)
        assertTrue(buf.drop(len).all { it == 'x'.code.toByte() })
    }

    @Test
    fun hyphenatedTooSmall() {
        assertFailsWith<IllegalArgumentException> {
            Uuid.nil().hyphenated().encodeLower(ByteArray(35))
        }
    }

    @Test
    fun simpleTooSmall() {
        assertFailsWith<IllegalArgumentException> {
            Uuid.nil().simple().encodeLower(ByteArray(31))
        }
    }

    @Test
    fun urnTooSmall() {
        assertFailsWith<IllegalArgumentException> {
            Uuid.nil().urn().encodeLower(ByteArray(44))
        }
    }

    @Test
    fun bracedTooSmall() {
        assertFailsWith<IllegalArgumentException> {
            Uuid.nil().braced().encodeLower(ByteArray(37))
        }
    }

    @Test
    fun hyphenatedToInner() {
        val hyphenated = Uuid.nil().hyphenated()
        assertEquals(Uuid.from(hyphenated), Uuid.nil())
    }

    @Test
    fun simpleToInner() {
        val simple = Uuid.nil().simple()
        assertEquals(Uuid.from(simple), Uuid.nil())
    }

    @Test
    fun urnToInner() {
        val urn = Uuid.nil().urn()
        assertEquals(Uuid.from(urn), Uuid.nil())
    }

    @Test
    fun bracedToInner() {
        val braced = Uuid.nil().braced()
        assertEquals(Uuid.from(braced), Uuid.nil())
    }
}
