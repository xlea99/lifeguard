package net.xlea99.lifeguard.entry.zetter_hovered_paintings_stack;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ZetterHoveredPaintingsStack {
    /**
     * None: two copies of one Zetter painting stopped stacking once you'd hovered one of them. A painting item caches
     * its size, name and author but never its resolution (PaintingItem.storePaintingData skips it, though
     * CanvasItem.storeCanvasData writes it for canvases), and CanvasItem.getResolution, a getter, WRITES the server's
     * current default resolution into the stack when it's missing. Zetter's tooltip preview calls it: hovering changed
     * the item, on that client only (creative then sends the client's copy to the server). Proven in Deep End
     * 2026-11-01 (user: a fresh drop has no CachedResolution, the hovered stack has 16, the server's copy of the drop
     * still has none after hovering, then the client merges them). Now every painting stores its REAL resolution when
     * it's made, as canvases do, so the getter never writes and the cache is correct (it used to pin whatever the
     * config said at first hover). Virtual paintings keep their canonical x16 (zettify_all_paintings).
     * And CanvasItem.getResolution is read-only: what still lacks a resolution (a blank combined canvas: the artist table
     * stores only its size) gets the default without it being written in, so blank canvases stack after hover too.
     * Runtime-gated: storePaintingData runs on both sides. (Zetter 0.21.7.)
     */
    public static final Entry ENTRY = Entries.fix("zetter_hovered_paintings_stack", Taste.NONE).home("zetter")
        .requiresMods("zetter")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private ZetterHoveredPaintingsStack() {
    }
}
