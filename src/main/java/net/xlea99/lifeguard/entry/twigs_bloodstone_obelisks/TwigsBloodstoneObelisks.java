package net.xlea99.lifeguard.entry.twigs_bloodstone_obelisks;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class TwigsBloodstoneObelisks {
    /** One mixin on vanilla's structure template loading, for Twigs' templates only. */
    public static final Entry ENTRY = Entries.fix("twigs_bloodstone_obelisks", Taste.NONE)
        .requiresMods("twigs")
        .side(Side.SERVER).restart(Restart.GAME);

    private TwigsBloodstoneObelisks() {
    }
}
