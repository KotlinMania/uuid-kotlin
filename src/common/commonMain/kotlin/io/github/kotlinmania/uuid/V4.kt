// port-lint: source v4.rs
package io.github.kotlinmania.uuid

/**
 * Creates a random UUID (Version 4).
 */
public fun Uuid.Companion.newV4(): Uuid =
    Builder.fromRandomBytes(Rng.u128()).intoUuid()
