// port-lint: source uuid/src/rng.rs
package io.github.kotlinmania.uuid

import kotlin.random.Random
import kotlin.random.nextULong

internal object Rng {
    fun u16(): UShort = Random.nextInt(0, 0x10000).toUShort()

    fun u64(): ULong = Random.nextULong()

    fun u128(): ByteArray = Random.nextBytes(16)

    fun fill(dest: ByteArray) {
        Random.nextBytes(dest)
    }

    object RngImp {
        fun u16(): UShort = Rng.u16()

        fun u64(): ULong = Rng.u64()

        fun u128(): ByteArray = Rng.u128()
    }
}
