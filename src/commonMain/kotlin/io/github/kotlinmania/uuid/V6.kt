// port-lint: source v6.rs
package io.github.kotlinmania.uuid

/**
 * Creates a new version 6 UUID using the current system time and node ID.
 */
public fun Uuid.Companion.nowV6(nodeId: ByteArray): Uuid {
    val ts = Timestamp.now(Timestamp.sharedContext())
    return newV6(ts, nodeId)
}

/**
 * Creates a new version 6 UUID using the given timestamp and a node ID.
 */
public fun Uuid.Companion.newV6(ts: Timestamp, nodeId: ByteArray): Uuid {
    val (ticks, counter) = ts.toGregorian()
    return Builder.fromSortedGregorianTimestamp(ticks, counter, nodeId).intoUuid()
}
