// port-lint: tests v3.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertEquals

class V3Test {
    private data class Fixture(
        val ns: Uuid,
        val name: String,
        val expected: String,
    )

    private val fixtures = listOf(
        Fixture(Uuid.NAMESPACE_DNS, "example.org", "04738bdf-b25a-3829-a801-b21a1d25095b"),
        Fixture(Uuid.NAMESPACE_DNS, "rust-lang.org", "c6db027c-615c-3b4d-959e-1a917747ca5a"),
        Fixture(Uuid.NAMESPACE_DNS, "42", "5aab6e0c-b7d3-379c-92e3-2bfbb5572511"),
        Fixture(Uuid.NAMESPACE_DNS, "lorem ipsum", "4f8772e9-b59c-3cc9-91a9-5c823df27281"),
        Fixture(Uuid.NAMESPACE_URL, "example.org", "39682ca1-9168-3da2-a1bb-f4dbcde99bf9"),
        Fixture(Uuid.NAMESPACE_URL, "rust-lang.org", "7ed45aaf-e75b-3130-8e33-ee4d9253b19f"),
        Fixture(Uuid.NAMESPACE_URL, "42", "08998a0c-fcf4-34a9-b444-f2bfc15731dc"),
        Fixture(Uuid.NAMESPACE_URL, "lorem ipsum", "e55ad2e6-fb89-34e8-b012-c5dde3cd67f0"),
        Fixture(Uuid.NAMESPACE_OID, "example.org", "f14eec63-2812-3110-ad06-1625e5a4a5b2"),
        Fixture(Uuid.NAMESPACE_OID, "rust-lang.org", "6506a0ec-4d79-3e18-8c2b-f2b6b34f2b6d"),
        Fixture(Uuid.NAMESPACE_OID, "42", "ce6925a5-2cd7-327b-ab1c-4b375ac044e4"),
        Fixture(Uuid.NAMESPACE_OID, "lorem ipsum", "5dd8654f-76ba-3d47-bc2e-4d6d3a78cb09"),
        Fixture(Uuid.NAMESPACE_X500, "example.org", "64606f3f-bd63-363e-b946-fca13611b6f7"),
        Fixture(Uuid.NAMESPACE_X500, "rust-lang.org", "bcee7a9c-52f1-30c6-a3cc-8c72ba634990"),
        Fixture(Uuid.NAMESPACE_X500, "42", "c1073fa2-d4a6-3104-b21d-7a6bdcf39a23"),
        Fixture(Uuid.NAMESPACE_X500, "lorem ipsum", "02f09a3f-1624-3b1d-8409-44eff7708208"),
    )

    @Test
    fun testNew() {
        for (fixture in fixtures) {
            val uuid = Uuid.newV3(fixture.ns, fixture.name.encodeToByteArray())
            assertEquals(Version.Md5, uuid.getVersion())
            assertEquals(Variant.RFC4122, uuid.getVariant())
        }
    }

    @Test
    fun testHyphenatedString() {
        for (fixture in fixtures) {
            val uuid = Uuid.newV3(fixture.ns, fixture.name.encodeToByteArray())
            assertEquals(fixture.expected, uuid.hyphenated().toString())
        }
    }
}
