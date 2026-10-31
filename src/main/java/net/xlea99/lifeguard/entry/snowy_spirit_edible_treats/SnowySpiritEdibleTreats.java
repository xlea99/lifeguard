package net.xlea99.lifeguard.entry.snowy_spirit_edible_treats;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SnowySpiritEdibleTreats {
    /** Food is an item's default component, set at startup: client and server must agree. */
    public static final Entry ENTRY = Entries.tweak("snowy_spirit_edible_treats", Taste.STRONG).home("snowyspirit")
        .requiresMods("snowyspirit")
        .usesIds("mob_effect", "alexscaves:sugar_rush")
        .side(Side.SERVER).restart(Restart.GAME).registryShaping();

    private SnowySpiritEdibleTreats() {
    }
}
