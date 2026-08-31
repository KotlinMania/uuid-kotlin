// port-lint: source uuid/src/external/slog_support.rs
package io.github.kotlinmania.uuid.external

import io.github.kotlinmania.uuid.NonNilUuid
import io.github.kotlinmania.uuid.Uuid

/**
 * Structured logging support for UUID.
 */
public object SlogSupport {
    public fun formatLogValue(uuid: Uuid): String = uuid.toString()

    public fun formatLogValue(nonNil: NonNilUuid): String = nonNil.toString()
}
