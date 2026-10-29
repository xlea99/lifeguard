package net.xlea99.lifeguard.entry.twigs_gravel_pebbles;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class TwigsGravelPebbles {
    /** Data only: NeoForge's {@code add_table} on gravel, with Twigs 1.20.1's pool (20%, 1 to 3, not with Silk Touch); Twigs 3.1.2's own pool never matches the gravel table. */
    public static final Entry ENTRY = Entries.fix("twigs_gravel_pebbles", Taste.NONE)
        .requiresMods("twigs")
        .side(Side.SERVER).restart(Restart.GAME);

    private TwigsGravelPebbles() {
    }
}
