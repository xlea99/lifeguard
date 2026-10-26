package net.xlea99.lifeguard.entry.bucket_mob_flicker;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class BucketMobFlicker {
    /** Buckets let mobs out on the server. */
    public static final Entry ENTRY = Entries.fix("bucket_mob_flicker", Taste.NONE)
        .side(Side.SERVER).restart(Restart.GAME);

    private BucketMobFlicker() {
    }
}
