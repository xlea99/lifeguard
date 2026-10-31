package net.xlea99.lifeguard.entry.dye_depot_runes;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

/**
 * Pure data, no code: Quark already makes a rune color for every dye, Dye Depot's included, so only the recipes
 * ({@code data/lifeguard/recipe/dye_depot_runes}, gated by this entry), the glint textures and the names were missing.
 * The textures and names always ship: they only show on an item that already carries one of these runes.
 */
public final class DyeDepotRunes {
    public static final Entry ENTRY = Entries.compat("dye_depot_runes", Taste.NONE).home("quark")
        .requiresMods("quark", "dye_depot")
        .side(Side.SERVER).restart(Restart.GAME);

    private DyeDepotRunes() {
    }
}
