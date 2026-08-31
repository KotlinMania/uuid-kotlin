// port-lint: source uuid/src/sha1.rs
package io.github.kotlinmania.uuid

internal object Sha1 {
    fun hash(ns: ByteArray, src: ByteArray): ByteArray {
        val totalLen = ns.size + src.size
        val bitLen = totalLen.toLong() * 8L

        val padLen = if (totalLen % 64 < 56) 56 - (totalLen % 64) else 120 - (totalLen % 64)
        val buffer = ByteArray(totalLen + padLen + 8)

        ns.copyInto(buffer, 0)
        src.copyInto(buffer, ns.size)
        buffer[totalLen] = 0x80.toByte()

        for (i in 0 until 8) {
            buffer[totalLen + padLen + i] = ((bitLen ushr ((7 - i) * 8)) and 0xFFL).toByte()
        }

        var h0 = 0x67452301u
        var h1 = 0xEFCDAB89u
        var h2 = 0x98BADCFEu
        var h3 = 0x10325476u
        var h4 = 0xC3D2E1F0u

        val w = UIntArray(80)

        for (chunkOffset in 0 until buffer.size step 64) {
            for (i in 0 until 16) {
                val byteOffset = chunkOffset + i * 4
                w[i] =
                    (
                        ((buffer[byteOffset].toInt() and 0xFF) shl 24) or
                            ((buffer[byteOffset + 1].toInt() and 0xFF) shl 16) or
                            ((buffer[byteOffset + 2].toInt() and 0xFF) shl 8) or
                            (buffer[byteOffset + 3].toInt() and 0xFF)
                    ).toUInt()
            }

            for (i in 16 until 80) {
                w[i] = (w[i - 3] xor w[i - 8] xor w[i - 14] xor w[i - 16]).rotateLeft(1)
            }

            var a = h0
            var b = h1
            var c = h2
            var d = h3
            var e = h4

            for (i in 0 until 80) {
                val f: UInt
                val k: UInt
                when (i) {
                    in 0..19 -> {
                        f = (b and c) or (b.inv() and d)
                        k = 0x5A827999u
                    }
                    in 20..39 -> {
                        f = b xor c xor d
                        k = 0x6ED9EBA1u
                    }
                    in 40..59 -> {
                        f = (b and c) or (b and d) or (c and d)
                        k = 0x8F1BBCDCu
                    }
                    else -> {
                        f = b xor c xor d
                        k = 0xCA62C1D6u
                    }
                }

                val temp = a.rotateLeft(5) + f + e + k + w[i]
                e = d
                d = c
                c = b.rotateLeft(30)
                b = a
                a = temp
            }

            h0 += a
            h1 += b
            h2 += c
            h3 += d
            h4 += e
        }

        val result = ByteArray(16)
        writeUInt32Be(result, 0, h0)
        writeUInt32Be(result, 4, h1)
        writeUInt32Be(result, 8, h2)
        writeUInt32Be(result, 12, h3)
        return result
    }

    private fun writeUInt32Be(dest: ByteArray, offset: Int, value: UInt) {
        dest[offset] = ((value shr 24) and 0xFFu).toByte()
        dest[offset + 1] = ((value shr 16) and 0xFFu).toByte()
        dest[offset + 2] = ((value shr 8) and 0xFFu).toByte()
        dest[offset + 3] = (value and 0xFFu).toByte()
    }
}
