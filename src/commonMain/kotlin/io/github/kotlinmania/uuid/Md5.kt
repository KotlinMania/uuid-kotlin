// port-lint: source md5.rs
package io.github.kotlinmania.uuid

internal object Md5 {
    private val S =
        intArrayOf(
            7,
            12,
            17,
            22,
            7,
            12,
            17,
            22,
            7,
            12,
            17,
            22,
            7,
            12,
            17,
            22,
            5,
            9,
            14,
            20,
            5,
            9,
            14,
            20,
            5,
            9,
            14,
            20,
            5,
            9,
            14,
            20,
            4,
            11,
            16,
            23,
            4,
            11,
            16,
            23,
            4,
            11,
            16,
            23,
            4,
            11,
            16,
            23,
            6,
            10,
            15,
            21,
            6,
            10,
            15,
            21,
            6,
            10,
            15,
            21,
            6,
            10,
            15,
            21,
        )

    private val K =
        UIntArray(64) { i ->
            (kotlin.math.floor(kotlin.math.abs(kotlin.math.sin(i + 1.0)) * 4294967296.0).toLong() and 0xFFFFFFFFL).toUInt()
        }

    fun hash(ns: ByteArray, src: ByteArray): ByteArray {
        val totalLen = ns.size + src.size
        val bitLen = totalLen.toLong() * 8L

        val padLen = if (totalLen % 64 < 56) 56 - (totalLen % 64) else 120 - (totalLen % 64)
        val buffer = ByteArray(totalLen + padLen + 8)

        ns.copyInto(buffer, 0)
        src.copyInto(buffer, ns.size)
        buffer[totalLen] = 0x80.toByte()

        for (i in 0 until 8) {
            buffer[totalLen + padLen + i] = ((bitLen ushr (i * 8)) and 0xFFL).toByte()
        }

        var a0 = 0x67452301u
        var b0 = 0xefcdab89u
        var c0 = 0x98badcfeu
        var d0 = 0x10325476u

        val m = UIntArray(16)

        for (chunkOffset in 0 until buffer.size step 64) {
            for (i in 0 until 16) {
                val byteOffset = chunkOffset + i * 4
                m[i] = (buffer[byteOffset].toInt() and 0xFF).toUInt() or
                    ((buffer[byteOffset + 1].toInt() and 0xFF).toUInt() shl 8) or
                    ((buffer[byteOffset + 2].toInt() and 0xFF).toUInt() shl 16) or
                    ((buffer[byteOffset + 3].toInt() and 0xFF).toUInt() shl 24)
            }

            var a = a0
            var b = b0
            var c = c0
            var d = d0

            for (i in 0 until 64) {
                val f: UInt
                val g: Int
                when (i) {
                    in 0..15 -> {
                        f = (b and c) or (b.inv() and d)
                        g = i
                    }
                    in 16..31 -> {
                        f = (d and b) or (d.inv() and c)
                        g = (5 * i + 1) % 16
                    }
                    in 32..47 -> {
                        f = b xor c xor d
                        g = (3 * i + 5) % 16
                    }
                    else -> {
                        f = c xor (b or d.inv())
                        g = (7 * i) % 16
                    }
                }

                val temp = d
                d = c
                c = b
                b = b + (a + f + K[i] + m[g]).rotateLeft(S[i])
                a = temp
            }

            a0 += a
            b0 += b
            c0 += c
            d0 += d
        }

        val result = ByteArray(16)
        writeUInt32Le(result, 0, a0)
        writeUInt32Le(result, 4, b0)
        writeUInt32Le(result, 8, c0)
        writeUInt32Le(result, 12, d0)
        return result
    }

    private fun writeUInt32Le(dest: ByteArray, offset: Int, value: UInt) {
        dest[offset] = (value and 0xFFu).toByte()
        dest[offset + 1] = ((value shr 8) and 0xFFu).toByte()
        dest[offset + 2] = ((value shr 16) and 0xFFu).toByte()
        dest[offset + 3] = ((value shr 24) and 0xFFu).toByte()
    }
}
