package net.xlea99.lifeguard.entry.snowy_spirit_brighter_ginger;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SnowySpiritBrighterGinger {
    /** Only textures. */
    public static final Entry ENTRY = Entries.tweak("snowy_spirit_brighter_ginger", Taste.DEEP_END)
        .requiresMods("snowyspirit")
        .side(Side.CLIENT).restart(Restart.GAME);

    private SnowySpiritBrighterGinger() {
    }
}
