package net.xlea99.lifeguard.entry.zetter_painting_view_distance;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ZetterPaintingViewDistance {
    /**
     * None: Zetter's framed painting entity type is built with only {@code sized(1, 1)}, so it gets the default tracking
     * range (5 chunks) and update interval (3 ticks). Vanilla's painting: {@code clientTrackingRange(10)} and
     * {@code updateInterval(Integer.MAX_VALUE)}. So Zetter paintings vanished before vanilla ones as you walked away
     * (user-confirmed 2026-11-01: "a bit before", as vanilla's 10 is capped by the view distance), and the server kept
     * sending position updates for an entity that never moves (the packets behind zetter_paintings_stay_put's bug).
     * Now it's built like vanilla's. (Zetter 0.21.7, ZetterEntities.)
     */
    public static final Entry ENTRY = Entries.fix("zetter_painting_view_distance", Taste.NONE).home("zetter")
        .requiresMods("zetter")
        .side(Side.SERVER).restart(Restart.GAME);

    private ZetterPaintingViewDistance() {
    }
}
