package net.xlea99.lifeguard.entry.dye_depot_sparklers;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class DyeDepotSparklers {
    /**
     * Runtime-gated: the 16 sparklers are registered whenever both mods are installed, never by the switch (a world may
     * hold them), and dyeing a sparkler runs on both sides. Off, their recipes and creative tab entries go, and a Dye
     * Depot dye no longer recolors a sparkler.
     */
    public static final Entry ENTRY = Entries.compat("dye_depot_sparklers", Taste.NONE)
        .requiresMods("caverns_and_chasms", "dye_depot")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private DyeDepotSparklers() {
    }
}
