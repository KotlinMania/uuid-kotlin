package io.github.kotlinmania.uuid

/**
 * The version of the UUID, denoting the generating algorithm.
 */
public enum class Version(
    public val number: Int,
) {
    /**
     * The nil, all-zero UUID.
     */
    Nil(0),

    /**
     * Version 1: timestamp and node ID.
     */
    Mac(1),

    /**
     * Version 2: DCE Security.
     */
    Dce(2),

    /**
     * Version 3: MD5 hash.
     */
    Md5(3),

    /**
     * Version 4: random.
     */
    Random(4),

    /**
     * Version 5: SHA-1 hash.
     */
    Sha1(5),

    /**
     * Version 6: sortable timestamp and node ID.
     */
    SortMac(6),

    /**
     * Version 7: timestamp and random.
     */
    SortRand(7),

    /**
     * Version 8: custom.
     */
    Custom(8),

    /**
     * The max, all-ones UUID.
     */
    Max(0xff),
}
