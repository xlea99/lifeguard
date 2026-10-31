package net.xlea99.lifeguard.entry.amendments_lava_layers;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class AmendmentsLavaLayers {
    /** Cauldrons change on the server. */
    public static final Entry ENTRY = Entries.tweak("amendments_lava_layers", Taste.DEEP_END).home("amendments")
        .requiresMods("amendments")
        .side(Side.SERVER).restart(Restart.GAME);

    /** A cauldron of lava still heats Farmer's Delight's cooking pots and skillets, as vanilla's lava cauldron did. */
    public static final Entry COOKING_HEAT = Entries.compat("amendments_lava_layers_cooking_heat", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("farmersdelight")
        .side(Side.SERVER).restart(Restart.GAME);

    /** Faucets pour lava into an empty cauldron a layer at a time, not Supplementaries' whole bucket. */
    public static final Entry FAUCETS = Entries.tweak("amendments_lava_layers_faucets", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME);

    private AmendmentsLavaLayers() {
    }
}
