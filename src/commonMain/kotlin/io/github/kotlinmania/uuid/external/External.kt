// port-lint: source external.rs
package io.github.kotlinmania.uuid.external

/**
 * External serialization and logging integrations.
 */
public object External {
    public val arbitrary: ArbitrarySupport = ArbitrarySupport
    public val borsh: BorshSupport = BorshSupport
    public val serde: SerdeSupport = SerdeSupport
    public val slog: SlogSupport = SlogSupport
}
