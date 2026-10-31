package net.xlea99.lifeguard.entry.vanilla_backport_cc_resin_trims;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class VanillaBackportCcResinTrims {
    /**
     * Assets only (our pack): the armor trims atlas gets a vanilla paletted_permutations source with resin for Caverns &
     * Chasms' 16 trim pattern textures. VB adds resin by patching the atlas's sources, and C&C's patterns come in through
     * Blueprint's own paletted_permutations source, which VB's patch skips, so resin on a C&C pattern drew nothing. A pack,
     * not loose assets: without VB the resin palette is missing and every pattern would log an error at each reload.
     */
    public static final Entry ENTRY = Entries.compat("vanilla_backport_cc_resin_trims", Taste.NONE)
        .home("vanillabackport")
        .requiresMods("vanillabackport", "caverns_and_chasms")
        .side(Side.CLIENT).restart(Restart.GAME)
        .resourcePack();

    private VanillaBackportCcResinTrims() {
    }
}
