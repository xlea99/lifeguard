package net.xlea99.lifeguard.entry.moonlight_provider_race;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class MoonlightProviderRace {
    /**
     * The race happens in every game, client and dedicated server alike, while mods are constructed: long before any
     * world, so it's load-gated and read once. Server side so a dedicated server reads it too.
     */
    public static final Entry ENTRY = Entries.fix("moonlight_provider_race", Taste.NONE)
        .requiresMods("moonlight")
        .side(Side.SERVER).restart(Restart.GAME);

    private MoonlightProviderRace() {
    }
}
