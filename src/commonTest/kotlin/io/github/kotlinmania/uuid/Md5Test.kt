// port-lint: tests md5.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertEquals

class Md5Test {
    @Test
    fun testMd5Hash() {
        val ns = ByteArray(16)
        val src = "hello".encodeToByteArray()
        val hash = Md5.hash(ns, src)
        assertEquals(16, hash.size)
    }
}
