package io.github.kotlinmania.uuid

/**
 * The reserved variants of UUIDs.
 */
public enum class Variant {
    /**
     * Reserved by the NCS for backward compatibility.
     */
    NCS,

    /**
     * As described in RFC 9562.
     */
    RFC4122,

    /**
     * Reserved by Microsoft for backward compatibility.
     */
    Microsoft,

    /**
     * Reserved for future expansion.
     */
    Future,
}
