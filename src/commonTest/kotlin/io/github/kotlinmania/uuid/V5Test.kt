// port-lint: tests v5.rs
package io.github.kotlinmania.uuid

import kotlin.test.Test
import kotlin.test.assertEquals

class V5Test {
    private data class Fixture(
        val ns: Uuid,
        val name: String,
        val expected: String,
    )

    private val fixtures = listOf(
        Fixture(Uuid.NAMESPACE_DNS, "example.org", "aad03681-8b63-5304-89e0-8ca8f49461b5"),
        Fixture(Uuid.NAMESPACE_DNS, "rust-lang.org", "c66bbb60-d62e-5f17-a399-3a0bd237c503"),
        Fixture(Uuid.NAMESPACE_DNS, "42", "7c411b5e-9d3f-50b5-9c28-62096e41c4ed"),
        Fixture(Uuid.NAMESPACE_DNS, "lorem ipsum", "97886a05-8a68-5743-ad55-56ab2d61cf7b"),
        Fixture(Uuid.NAMESPACE_URL, "example.org", "54a35416-963c-5dd6-a1e2-5ab7bb5bafc7"),
        Fixture(Uuid.NAMESPACE_URL, "rust-lang.org", "c48d927f-4122-5413-968c-598b1780e749"),
        Fixture(Uuid.NAMESPACE_URL, "42", "5c2b23de-4bad-58ee-a4b3-f22f3b9cfd7d"),
        Fixture(Uuid.NAMESPACE_URL, "lorem ipsum", "15c67689-4b85-5253-86b4-49fbb138569f"),
        Fixture(Uuid.NAMESPACE_OID, "example.org", "34784df9-b065-5094-92c7-00bb3da97a30"),
        Fixture(Uuid.NAMESPACE_OID, "rust-lang.org", "8ef61ecb-977a-5844-ab0f-c25ef9b8d5d6"),
        Fixture(Uuid.NAMESPACE_OID, "42", "ba293c61-ad33-57b9-9671-f3319f57d789"),
        Fixture(Uuid.NAMESPACE_OID, "lorem ipsum", "6485290d-f79e-5380-9e64-cb4312c7b4a6"),
        Fixture(Uuid.NAMESPACE_X500, "example.org", "e3635e86-f82b-5bbc-a54a-da97923e5c76"),
        Fixture(Uuid.NAMESPACE_X500, "rust-lang.org", "26c9c3e9-49b7-56da-8b9f-a0fb916a71a3"),
        Fixture(Uuid.NAMESPACE_X500, "42", "e4b88014-47c6-5fe0-a195-13710e5f6e27"),
        Fixture(Uuid.NAMESPACE_X500, "lorem ipsum", "b11f79a5-1e6d-57ce-a4b5-ba8531ea03d0"),
    )

    @Test
    fun testGetVersion() {
        val uuid = Uuid.newV5(Uuid.NAMESPACE_DNS, "rust-lang.org".encodeToByteArray())
        assertEquals(Version.Sha1, uuid.getVersion())
        assertEquals(5, uuid.getVersionNum())
    }

    @Test
    fun testHyphenated() {
        for (fixture in fixtures) {
            val uuid = Uuid.newV5(fixture.ns, fixture.name.encodeToByteArray())
            assertEquals(fixture.expected, uuid.hyphenated().toString())
        }
    }

    @Test
    fun testNew() {
        for (fixture in fixtures) {
            val uuid = Uuid.newV5(fixture.ns, fixture.name.encodeToByteArray())
            assertEquals(Version.Sha1, uuid.getVersion())
            assertEquals(Variant.RFC4122, uuid.getVariant())
            assertEquals(uuid, Uuid.parseStr(fixture.expected))
        }
    }
}
