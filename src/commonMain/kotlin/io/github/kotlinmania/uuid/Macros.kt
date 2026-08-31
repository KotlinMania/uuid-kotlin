// port-lint: source uuid/src/macros.rs
package io.github.kotlinmania.uuid

/**
 * Compile-time UUID construction helper.
 *
 * In Kotlin, compile-time literal parsing is performed via companion parsing or constants.
 */
public object Macros {
    /**
     * Parses a UUID from a string literal.
     */
    public fun uuid(str: String): Uuid = Uuid.parseStr(str)
}
