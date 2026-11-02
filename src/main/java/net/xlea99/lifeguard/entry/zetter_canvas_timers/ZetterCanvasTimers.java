package net.xlea99.lifeguard.entry.zetter_canvas_timers;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ZetterCanvasTimers {
    /**
     * None: Zetter's client feeds CanvasRenderer.update the wall clock in MILLISECONDS (Util.getMillis(), every client
     * tick), but its timers count TICKS: a painting's texture unloaded 3.6 s after you looked away (meant: 3600 ticks,
     * 3 minutes) and was re-requested from the server when you looked back (a brief flicker; user-confirmed 2026-11-01),
     * and a texture request timed out after 30 ms (meant: 1.5 s). Now it's fed the same clock in ticks (millis / 50).
     * (Zetter 0.21.7, ZetterClientModEvents.onRenderTickStart.)
     */
    public static final Entry ENTRY = Entries.fix("zetter_canvas_timers", Taste.NONE).home("zetter")
        .requiresMods("zetter")
        .side(Side.CLIENT).restart(Restart.GAME);

    private ZetterCanvasTimers() {
    }
}
