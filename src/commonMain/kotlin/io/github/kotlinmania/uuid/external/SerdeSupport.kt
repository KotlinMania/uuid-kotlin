// port-lint: source uuid/src/external/serde_support.rs
package io.github.kotlinmania.uuid.external

import io.github.kotlinmania.uuid.NonNilUuid
import io.github.kotlinmania.uuid.Uuid
import io.github.kotlinmania.uuid.hyphenated

/**
 * Serialization support for UUID.
 */
public object SerdeSupport {
    public fun serializeUuid(uuid: Uuid, humanReadable: Boolean = true): String =
        if (humanReadable) uuid.hyphenated().toString() else uuid.asBytes().decodeToString()

    public fun serializeNonNilUuid(nonNil: NonNilUuid, humanReadable: Boolean = true): String =
        serializeUuid(nonNil.get(), humanReadable)
}
