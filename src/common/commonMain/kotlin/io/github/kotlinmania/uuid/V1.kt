// port-lint: source v1.rs
package io.github.kotlinmania.uuid

/**
 * Creates a new version 1 UUID using the current system time and node ID.
 */
public fun Uuid.Companion.nowV1(nodeId: ByteArray): Uuid {
    val ts = Timestamp.now(Timestamp.sharedContext())
    return newV1(ts, nodeId)
}

/**
 * Creates a new version 1 UUID using the given timestamp and node ID.
 */
public fun Uuid.Companion.newV1(ts: Timestamp, nodeId: ByteArray): Uuid {
    val (ticks, counter) = ts.toGregorian()
    return Builder.fromGregorianTimestamp(ticks, counter, nodeId).intoUuid()
}
