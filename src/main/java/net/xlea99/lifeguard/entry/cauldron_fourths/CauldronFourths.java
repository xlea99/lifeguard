package net.xlea99.lifeguard.entry.cauldron_fourths;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CauldronFourths {
    /**
     * Water and powder snow cauldrons (and every block sharing their level) hold a bucket as four levels, a bottle as
     * one, the unit of every Moonlight container. It widens a vanilla block property, so clients must match the server.
     */
    public static final Entry ENTRY = Entries.tweak("cauldron_fourths", Taste.DEEP_END)
        .requiresMods("moonlight")
        .side(Side.SERVER).restart(Restart.GAME)
        .registryShaping().resourcePack();

    // Each mod that assumed three levels, taught four. Off, that mod keeps its threes and misbehaves against four-level
    // cauldrons: the honest cost of turning a compat off, said in each one's description.

    /** Water at four levels, crafting in cauldrons, the dye cauldron, and Amendments' cauldron models. */
    public static final Entry AMENDMENTS = Entries.compat("amendments", Taste.NONE).parent(ENTRY)
        .requiresMods("amendments")
        .side(Side.SERVER).restart(Restart.GAME)
        .resourcePack();

    /** Faucets and moved cauldrons fill to four. */
    public static final Entry SUPPLEMENTARIES = Entries.compat("supplementaries", Taste.NONE).parent(ENTRY)
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME);

    /** Milk and milkshake cauldrons at four levels, with their models. */
    public static final Entry NEAPOLITAN = Entries.compat("neapolitan", Taste.NONE).parent(ENTRY)
        .requiresMods("neapolitan")
        .side(Side.SERVER).restart(Restart.GAME)
        .resourcePack();

    /** Golden buckets pour and scoop full cauldrons at four. */
    public static final Entry CAVERNS_AND_CHASMS = Entries.compat("caverns_and_chasms", Taste.NONE).parent(ENTRY)
        .requiresMods("caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME);

    /** The Tremorsaurus' ripple sits on a cauldron's four-level surface. */
    public static final Entry ALEXS_CAVES = Entries.compat("alexs_caves", Taste.NONE).parent(ENTRY)
        .requiresMods("alexscaves")
        .side(Side.SERVER).restart(Restart.GAME);

    private CauldronFourths() {
    }
}
