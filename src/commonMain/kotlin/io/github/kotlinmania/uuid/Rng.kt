// port-lint: source rng.rs
package io.github.kotlinmania.uuid

import kotlin.random.Random
import kotlin.random.nextULong

internal object Rng {
    fun u16(): UShort = Random.nextInt(0, 0x10000).toUShort()

    fun u64(): ULong = Random.nextULong()
}
