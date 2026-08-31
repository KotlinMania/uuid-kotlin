// port-lint: source uuid/src/external/arbitrary_support.rs
package io.github.kotlinmania.uuid.external

import io.github.kotlinmania.uuid.Builder
import io.github.kotlinmania.uuid.NonNilUuid
import io.github.kotlinmania.uuid.Uuid

/**
 * Arbitrary data generation support for UUID types.
 */
public object ArbitrarySupport {
    public fun arbitraryUuid(bytes: ByteArray): Uuid {
        require(bytes.size >= 16) { "Not enough data" }
        return Builder.fromRandomBytes(bytes.copyOfRange(0, 16)).intoUuid()
    }

    public fun arbitraryNonNilUuid(bytes: ByteArray): NonNilUuid? {
        val uuid = arbitraryUuid(bytes)
        return NonNilUuid.new(uuid)
    }
}
