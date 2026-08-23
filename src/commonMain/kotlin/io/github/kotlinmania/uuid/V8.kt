// port-lint: source v8.rs
package io.github.kotlinmania.uuid

/**
 * Creates a custom UUID comprised almost entirely of user-supplied bytes (Version 8).
 */
public fun Uuid.Companion.newV8(buf: ByteArray): Uuid {
    require(buf.size == 16) { "expected 16 bytes for v8 UUID, found ${buf.size}" }
    return Builder.fromCustomBytes(buf).intoUuid()
}
