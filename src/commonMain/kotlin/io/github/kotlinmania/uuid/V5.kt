// port-lint: source v5.rs
package io.github.kotlinmania.uuid

/**
 * Creates a UUID using a name from a namespace, based on the SHA-1 hash.
 *
 * A number of namespaces are available as constants on [Uuid]:
 * - [Uuid.NAMESPACE_DNS]
 * - [Uuid.NAMESPACE_OID]
 * - [Uuid.NAMESPACE_URL]
 * - [Uuid.NAMESPACE_X500]
 */
public fun Uuid.Companion.newV5(namespace: Uuid, name: ByteArray): Uuid =
    Builder.fromSha1Bytes(Sha1.hash(namespace.asBytes(), name)).intoUuid()
