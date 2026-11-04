package net.xlea99.lifeguard.entry.citadel_iris_post_effects;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CitadelIrisPostEffects {
    /** Only when entities are drawn within the frame. */
    public static final Entry ENTRY = Entries.compat("citadel_iris_post_effects", Taste.NONE)
        .home("citadel")
        .requiresMods("citadel", "iris")
        .side(Side.CLIENT).restart(Restart.GAME);

    private CitadelIrisPostEffects() {
    }
}
