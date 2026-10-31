package net.xlea99.lifeguard.entry.cc_extended_plated_bricks;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcExtendedPlatedBricks {
    /**
     * Caverns & Chasms' plated bricks in netherite and necromium. Runtime-gated: each metal's bricks are registered
     * whenever its entry is installed, never by the switch (a world may hold them); off, they leave the creative tab and
     * can't be crafted. See block/PlatedBricks.
     */
    public static final Entry ENTRY = Entries.compat("cc_extended_plated_bricks", Taste.LIGHT)
        .requiresMods("caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Azure and scarlet neodymium, together. Their bricks are magnets (magnet/Magnets): off, they stop pushing and pulling. */
    public static final Entry ALEXSCAVES = Entries.compat("alexscaves", Taste.LIGHT).parent(ENTRY)
        .requiresMods("alexscaves")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated()
        .sharesMixins("ac_magnets", "alexscaves");

    /** Etcetera's bismuth. */
    public static final Entry ETCETERA = Entries.compat("etcetera", Taste.LIGHT).parent(ENTRY)
        .requiresMods("etcetera")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private CcExtendedPlatedBricks() {
    }
}
