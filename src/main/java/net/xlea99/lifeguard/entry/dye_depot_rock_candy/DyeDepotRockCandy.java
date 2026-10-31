package net.xlea99.lifeguard.entry.dye_depot_rock_candy;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class DyeDepotRockCandy {
    /**
     * The 16 candies are registered whenever both mods are installed, never by the switch: a world may hold them. Off,
     * their recipes and creative tab entries go, and the Confection Oven stops reading them.
     */
    public static final Entry ENTRY = Entries.compat("dye_depot_rock_candy", Taste.NONE).home("alexscaves")
        .requiresMods("alexscaves", "dye_depot")
        .usesIds("block", "alexscaves:rock_candy_white")
        .usesIds("item", "alexscaves:rock_candy_white", "alexscaves:rock_candy_black")
        .usesIds("creative_mode_tab", "alexscaves:candy_cavity")
        .side(Side.SERVER).restart(Restart.GAME);

    /** Where they turn up in the world; the parent only makes them craftable. */
    public static final Entry CANDY_CAVITY = Entries.compat("dye_depot_rock_candy_candy_cavity", Taste.STRONG)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .side(Side.SERVER).restart(Restart.GAME);

    private DyeDepotRockCandy() {
    }
}
