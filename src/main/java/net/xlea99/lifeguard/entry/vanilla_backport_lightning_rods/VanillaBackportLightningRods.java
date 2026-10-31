package net.xlea99.lifeguard.entry.vanilla_backport_lightning_rods;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class VanillaBackportLightningRods {
    /** Lightning picks its target on the server. */
    public static final Entry ENTRY = Entries.fix("vanilla_backport_lightning_rods", Taste.NONE)
        .requiresMods("vanillabackport")
        .side(Side.SERVER).restart(Restart.GAME);

    private VanillaBackportLightningRods() {
    }
}
