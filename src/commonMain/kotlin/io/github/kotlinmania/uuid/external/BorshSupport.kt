// port-lint: source external/borsh_support.rs
package io.github.kotlinmania.uuid.external

import io.github.kotlinmania.uuid.Uuid

/**
 * Borsh binary serialization support for UUID.
 */
public object BorshSupport {
    public fun toBytes(uuid: Uuid): ByteArray = uuid.asBytes()

    public fun fromBytes(bytes: ByteArray): Uuid = Uuid.fromBytes(bytes)
}
