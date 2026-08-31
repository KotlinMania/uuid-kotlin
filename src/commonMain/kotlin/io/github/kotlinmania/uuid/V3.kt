// port-lint: source v3.rs
package io.github.kotlinmania.uuid

/**
 * Creates a UUID using a name from a namespace, based on the MD5 hash.
 *
 * A number of namespaces are available as constants on [Uuid]:
 * - [Uuid.NAMESPACE_DNS]
 * - [Uuid.NAMESPACE_OID]
 * - [Uuid.NAMESPACE_URL]
 * - [Uuid.NAMESPACE_X500]
 */
public fun Uuid.Companion.newV3(namespace: Uuid, name: ByteArray): Uuid =
    Builder.fromMd5Bytes(Md5.hash(namespace.asBytes(), name)).intoUuid()
