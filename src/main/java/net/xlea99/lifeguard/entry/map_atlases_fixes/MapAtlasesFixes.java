package net.xlea99.lifeguard.entry.map_atlases_fixes;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class MapAtlasesFixes {
    /** The server makes the cartography and crafting results; clients only predict them. */
    public static final Entry ENTRY = Entries.fix("map_atlases_fixes", Taste.NONE).home("map_atlases")
        .requiresMods("map_atlases")
        .side(Side.SERVER).restart(Restart.GAME);

    private MapAtlasesFixes() {
    }
}
