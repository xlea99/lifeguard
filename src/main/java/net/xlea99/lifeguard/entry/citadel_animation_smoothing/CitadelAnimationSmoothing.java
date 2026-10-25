package net.xlea99.lifeguard.entry.citadel_animation_smoothing;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CitadelAnimationSmoothing {
    /** Rendering only. */
    public static final Entry ENTRY = Entries.fix("citadel_animation_smoothing", Taste.NONE)
        .requiresMods("citadel")
        .side(Side.CLIENT).restart(Restart.GAME);

    private CitadelAnimationSmoothing() {
    }
}
