package net.xlea99.lifeguard.entry.cc_crafted_tether_potions;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcCraftedTetherPotions {
    /**
     * Runtime-gated: the recipe type is registered whenever Caverns & Chasms is installed, and the brewing list is built
     * on both sides (the client builds its own on joining), each asking the switch as it does.
     */
    public static final Entry ENTRY = Entries.tweak("cc_crafted_tether_potions", Taste.DEEP_END)
        .requiresMods("caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** The crafting recipe, one per potion and bottle, in EMI (which can't list a recipe that works out its result). */
    public static final Entry EMI = Entries.compat("emi", Taste.NONE).parent(ENTRY)
        .requiresMods("emi")
        .side(Side.CLIENT).restart(Restart.GAME);

    private CcCraftedTetherPotions() {
    }
}
