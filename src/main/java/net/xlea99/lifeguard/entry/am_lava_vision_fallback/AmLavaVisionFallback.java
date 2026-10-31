package net.xlea99.lifeguard.entry.am_lava_vision_fallback;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class AmLavaVisionFallback {
    /** Brewing recipes are built on both sides from common config; the server's brewing stands are what count. */
    public static final Entry ENTRY = Entries.tweak("am_lava_vision_fallback", Taste.DEEP_END).home("alexsmobs")
        .requiresMods("alexsmobs")
        .side(Side.SERVER).restart(Restart.GAME);

    private AmLavaVisionFallback() {
    }
}
