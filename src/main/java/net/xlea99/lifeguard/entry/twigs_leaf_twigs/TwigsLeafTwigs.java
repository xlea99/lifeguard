package net.xlea99.lifeguard.entry.twigs_leaf_twigs;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class TwigsLeafTwigs {
    /** Data only: Twigs' own loot modifier ({@code twigs:twig_loot_modifier}), listed at NeoForge's path (Twigs lists it at Forge's, which NeoForge never reads). */
    public static final Entry ENTRY = Entries.fix("twigs_leaf_twigs", Taste.NONE)
        .requiresMods("twigs")
        .side(Side.SERVER).restart(Restart.GAME);

    private TwigsLeafTwigs() {
    }
}
