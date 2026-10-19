// port-lint: source rng.rs
package io.github.kotlinmania.uuid

import io.github.kotlinmania.getrandom.GetrandomResult
import io.github.kotlinmania.getrandom.getrandom

internal interface RngTrait {
    fun u128(): ByteArray

    fun u64(): ULong

    fun u16(): UShort
}

internal fun u128(): ByteArray = RngImp.u128()

internal fun u64(): ULong = RngImp.u64()

internal fun u16(): UShort = RngImp.u16()

internal object RngImp : RngTrait {
    override fun u128(): ByteArray {
        val bytes = ByteArray(16)
        when (val res = getrandom(bytes)) {
            is GetrandomResult.Ok -> {}
            is GetrandomResult.Failure -> error("could not retrieve random bytes for uuid: ${res.error}")
        }
        return bytes
    }

    override fun u64(): ULong {
        val bytes = ByteArray(8)
        when (val res = getrandom(bytes)) {
            is GetrandomResult.Ok -> {}
            is GetrandomResult.Failure -> error("could not retrieve random bytes for uuid: ${res.error}")
        }
        var v = 0UL
        for (i in 0 until 8) {
            v = v or ((bytes[i].toLong() and 0xFFL).toULong() shl (i * 8))
        }
        return v
    }

    override fun u16(): UShort {
        val bytes = ByteArray(2)
        when (val res = getrandom(bytes)) {
            is GetrandomResult.Ok -> {}
            is GetrandomResult.Failure -> error("could not retrieve random bytes for uuid: ${res.error}")
        }
        val v = (bytes[0].toInt() and 0xFF) or ((bytes[1].toInt() and 0xFF) shl 8)
        return v.toUShort()
    }
}

internal object Rng {
    fun u16(): UShort = RngImp.u16()

    fun u64(): ULong = RngImp.u64()

    fun u128(): ByteArray = RngImp.u128()

    object RngImp {
        fun u16(): UShort =
            io.github.kotlinmania.uuid.RngImp
                .u16()

        fun u64(): ULong =
            io.github.kotlinmania.uuid.RngImp
                .u64()

        fun u128(): ByteArray =
            io.github.kotlinmania.uuid.RngImp
                .u128()
    }
}
