package net.xlea99.lifeguard.entry.vanilla_backport_fa_cold_pig;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class VanillaBackportFaColdPig {
    /** Only which of Fresh Animations' files a cold pig is drawn with, and only when Fresh Animations is selected. */
    public static final Entry ENTRY = Entries.compat("vanilla_backport_fa_cold_pig", Taste.NONE)
        .home("vanillabackport")
        .requiresMods("vanillabackport", "entity_model_features")
        .side(Side.CLIENT).restart(Restart.GAME);

    private VanillaBackportFaColdPig() {
    }
}
