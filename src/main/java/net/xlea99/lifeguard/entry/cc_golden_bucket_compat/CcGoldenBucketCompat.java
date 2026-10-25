package net.xlea99.lifeguard.entry.cc_golden_bucket_compat;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcGoldenBucketCompat {
    /** Tanks change on the server (the client predicts the same). */
    public static final Entry ENTRY = Entries.compat("cc_golden_bucket_compat", Taste.NONE)
        .requiresMods("caverns_and_chasms", "moonlight")
        .side(Side.SERVER).restart(Restart.GAME);

    /** A filled golden bucket pours into an empty cauldron as an Amendments cauldron, for liquids Caverns & Chasms doesn't pour itself. */
    public static final Entry AMENDMENTS = Entries.compat("amendments", Taste.NONE).parent(ENTRY)
        .requiresMods("amendments")
        .side(Side.SERVER).restart(Restart.GAME);

    private CcGoldenBucketCompat() {
    }
}
