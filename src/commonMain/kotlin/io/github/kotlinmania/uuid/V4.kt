// port-lint: source uuid/src/v4.rs
package io.github.kotlinmania.uuid

import kotlin.random.Random

/**
 * Creates a random UUID (Version 4).
 */
public fun Uuid.Companion.newV4(): Uuid =
    Builder.fromRandomBytes(Random.nextBytes(16)).intoUuid()
