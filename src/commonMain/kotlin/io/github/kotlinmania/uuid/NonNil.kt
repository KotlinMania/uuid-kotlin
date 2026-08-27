// port-lint: source non_nil.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.uuid

import kotlin.native.HiddenFromObjC

/**
 * A wrapper type for nil UUIDs that provides a more memory-efficient
 * `Option<NonNilUuid>` representation.
 */

/**
 * A UUID that is guaranteed not to be the [nil UUID](https://www.ietf.org/rfc/rfc9562.html#name-nil-uuid).
 *
 * This is useful for representing optional UUIDs more efficiently, as a nullable
 * [NonNilUuid] in Kotlin still carries the "no UUID" case without the wrapper
 * needing to encode it.
 *
 * Note that [Uuid]s created by the following methods are guaranteed to be non-nil:
 *
 * - `Uuid.newV1`
 * - `Uuid.nowV1`
 * - `Uuid.newV3`
 * - `Uuid.newV4`
 * - `Uuid.newV5`
 * - `Uuid.newV6`
 * - `Uuid.nowV6`
 * - `Uuid.newV7`
 * - `Uuid.nowV7`
 * - `Uuid.newV8`
 *
 * ## ABI
 *
 * The [NonNilUuid] type does not yet have a stable in-memory layout. Its
 * representation or alignment may change. It is currently only guaranteed
 * that [NonNilUuid] and a nullable [NonNilUuid] occupy the storage shape
 * Kotlin chooses for boxed reference types backed by a single [Uuid].
 */
public class NonNilUuid private constructor(
    private val inner: Uuid,
) : Comparable<NonNilUuid> {
    /**
     * Get the underlying [Uuid] value.
     */
    public fun get(): Uuid = inner

    override fun compareTo(other: NonNilUuid): Int = inner.compareTo(other.inner)

    override fun equals(other: Any?): Boolean = other is NonNilUuid && inner == other.inner

    override fun hashCode(): Int = inner.hashCode()

    override fun toString(): String = inner.toString()

    /**
     * Formats the wrapped UUID using its `Debug` shape, mirroring the upstream
     * `fmt::Debug` impl that delegates to `Uuid::from(*self)`.
     */
    public fun debug(): String = inner.toString()

    /**
     * Formats the wrapped UUID using its display shape.
     */
    public fun fmt(): String = inner.toString()

    /**
     * Compares this non-nil UUID with a plain [Uuid], mirroring upstream
     * `PartialEq<Uuid> for NonNilUuid`.
     */
    public fun equalsUuid(other: Uuid): Boolean = inner == other

    /**
     * Compares equality with another [Uuid].
     */
    public fun eq(other: Uuid): Boolean = inner == other

    /**
     * Compares this non-nil UUID with a plain [Uuid] using lexicographic byte
     * order, mirroring upstream `PartialOrd<Uuid> for NonNilUuid`.
     */
    public fun compareToUuid(other: Uuid): Int = inner.compareTo(other)

    /**
     * Compares ordering with another [Uuid].
     */
    public fun partialCmp(other: Uuid): Int = inner.compareTo(other)

    public companion object {
        /**
         * Creates a non-nil UUID if the value is non-nil.
         */
        public fun new(uuid: Uuid): NonNilUuid? = if (uuid.isNil()) null else NonNilUuid(uuid)

        /**
         * Creates a non-nil without checking whether the value is non-nil.
         * Callers must ensure the value is not nil; if it is, the resulting
         * wrapper will silently wrap the nil UUID and downstream code that
         * relies on the non-nil invariant will misbehave.
         */
        public fun newUnchecked(uuid: Uuid): NonNilUuid = NonNilUuid(uuid)

        /**
         * Converts a [NonNilUuid] back into a [Uuid].
         */
        public fun fromNonNil(nonNil: NonNilUuid): Uuid = nonNil.inner

        /**
         * Attempts to convert a [Uuid] into a [NonNilUuid].
         */
        @HiddenFromObjC
        public fun from(uuid: Uuid): Result<NonNilUuid> = tryFrom(uuid)

        /**
         * Attempts to convert a [Uuid] into a [NonNilUuid]. Returns a
         * `Result` whose error is [Error] when the input UUID is nil.
         */
        @HiddenFromObjC
        public fun tryFrom(uuid: Uuid): Result<NonNilUuid> =
            if (uuid.isNil()) {
                Result.failure(Error(ErrorKind.Nil))
            } else {
                Result.success(NonNilUuid(uuid))
            }
    }
}

/**
 * Compares a plain [Uuid] against a [NonNilUuid], mirroring upstream
 * `PartialOrd<NonNilUuid> for Uuid`.
 */
public fun Uuid.compareTo(other: NonNilUuid): Int = this.compareTo(other.get())

/**
 * Equality between a plain [Uuid] and a [NonNilUuid], mirroring upstream
 * `PartialEq<NonNilUuid> for Uuid`.
 */
public fun Uuid.equalsNonNil(other: NonNilUuid): Boolean = this == other.get()
