// port-lint: source non_nil.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NonNilTest {
    // The upstream `test_non_nil_with_option_size` test asserts that
    // `mem::size_of::<Option<NonNilUuid>>() == mem::size_of::<Uuid>()`. That
    // relies on Rust's niche layout for `NonZeroU128`, which has no Kotlin
    // analog: a nullable [NonNilUuid] in Kotlin is always a separate boxed
    // reference. The size relationship is not portable; it is intentionally
    // not asserted here.

    @Test
    fun testNonNil() {
        val uuid =
            Uuid.fromBytes(
                byteArrayOf(
                    0x01, 0x23, 0x45, 0x67,
                    0x89.toByte(), 0xab.toByte(), 0xcd.toByte(), 0xef.toByte(),
                    0x01, 0x23, 0x45, 0x67,
                    0x89.toByte(), 0xab.toByte(), 0xcd.toByte(), 0xef.toByte(),
                ),
            )

        assertEquals(uuid, NonNilUuid.tryFrom(uuid).getOrThrow().get())
        assertEquals(uuid, NonNilUuid.new(uuid)!!.get())
        assertEquals(uuid, NonNilUuid.newUnchecked(uuid).get())

        assertTrue(NonNilUuid.tryFrom(Uuid.nil()).isFailure)
        assertNull(NonNilUuid.new(Uuid.nil()))
    }

    @Test
    fun testNonNilFormatting() {
        val uuid =
            Uuid.fromBytes(
                byteArrayOf(
                    0x01, 0x23, 0x45, 0x67,
                    0x89.toByte(), 0xab.toByte(), 0xcd.toByte(), 0xef.toByte(),
                    0x01, 0x23, 0x45, 0x67,
                    0x89.toByte(), 0xab.toByte(), 0xcd.toByte(), 0xef.toByte(),
                ),
            )
        val nonNil = NonNilUuid.tryFrom(uuid).getOrThrow()

        assertEquals(uuid.toString(), nonNil.toString())
        assertEquals(uuid.toString(), nonNil.debug())
    }

    @Test
    fun testNonNilOrd() {
        val uuid1 =
            Uuid.fromBytes(
                byteArrayOf(
                    0x01, 0x23, 0x45, 0x67,
                    0x89.toByte(), 0xab.toByte(), 0xcd.toByte(), 0xef.toByte(),
                    0x01, 0x23, 0x45, 0x67,
                    0x89.toByte(), 0xab.toByte(), 0xcd.toByte(), 0xef.toByte(),
                ),
            )
        val uuid2 =
            Uuid.fromBytes(
                byteArrayOf(
                    0x01, 0x23, 0x45, 0x67,
                    0x89.toByte(), 0xab.toByte(), 0xcd.toByte(), 0xef.toByte(),
                    0x01, 0x23, 0x45, 0x67,
                    0x89.toByte(), 0xab.toByte(), 0xcd.toByte(), 0xf0.toByte(),
                ),
            )

        val nonNil1 = NonNilUuid.tryFrom(uuid1).getOrThrow()
        val nonNil2 = NonNilUuid.tryFrom(uuid2).getOrThrow()

        // Ordering between NonNilUuid instances
        assertTrue(nonNil1 < nonNil2)
        assertTrue(nonNil2 > nonNil1)

        // Ordering between NonNilUuid and Uuid
        assertTrue(nonNil1.compareToUuid(uuid2) < 0)
        assertTrue(nonNil2.compareToUuid(uuid1) > 0)

        // Ordering between Uuid and NonNilUuid
        assertTrue(uuid1.compareTo(nonNil2) < 0)
        assertTrue(uuid2.compareTo(nonNil1) > 0)

        // Equality between NonNilUuid instances
        val nonNil1Copy = NonNilUuid.tryFrom(uuid1).getOrThrow()
        assertEquals(nonNil1, nonNil1Copy)
        assertTrue(nonNil1 <= nonNil1Copy)
        assertTrue(nonNil1 >= nonNil1Copy)

        // Equality between NonNilUuid and Uuid
        assertTrue(nonNil1.equalsUuid(uuid1))
        assertTrue(uuid1.equalsNonNil(nonNil1))
        assertTrue(nonNil1.compareToUuid(uuid1) >= 0)
        assertTrue(nonNil1.compareToUuid(uuid1) <= 0)
        assertTrue(uuid1.compareTo(nonNil1) >= 0)
        assertTrue(uuid1.compareTo(nonNil1) <= 0)
    }

    @Test
    fun testNonNilFromAndUnchecked() {
        val uuid =
            Uuid.fromBytes(
                byteArrayOf(
                    0x01, 0x23, 0x45, 0x67,
                    0x89.toByte(), 0xab.toByte(), 0xcd.toByte(), 0xef.toByte(),
                    0x01, 0x23, 0x45, 0x67,
                    0x89.toByte(), 0xab.toByte(), 0xcd.toByte(), 0xef.toByte(),
                ),
            )

        val nonNil = NonNilUuid.tryFrom(uuid).getOrThrow()
        val converted = NonNilUuid.fromNonNil(nonNil)
        assertEquals(uuid, converted)

        val result = NonNilUuid.tryFrom(uuid)
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertFalse(result.getOrThrow().get().isNil())
    }
}
