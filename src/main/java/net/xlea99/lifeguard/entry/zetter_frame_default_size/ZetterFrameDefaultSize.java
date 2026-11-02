package net.xlea99.lifeguard.entry.zetter_frame_default_size;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ZetterFrameDefaultSize {
    /**
     * None: placing a framed painting item that has a painting code but no CachedBlockSize (a /give, a loot table or a
     * trade written by hand) crashed: FrameItem.useOn hands PaintingItem.getBlockSize's null (no data) or empty array
     * (key missing) straight to the PaintingEntity constructor, which reads [0] and [1]. Now a missing or malformed size
     * places as 1x1 (user, 2026-11-01: "just instantiate a default instead of crashing the game"). Items Zetter or
     * Lifeguard make always carry their size. Found by reading the code (audit B4). (Zetter 0.21.7, FrameItem.useOn.)
     */
    public static final Entry ENTRY = Entries.fix("zetter_frame_default_size", Taste.NONE).home("zetter")
        .requiresMods("zetter")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private ZetterFrameDefaultSize() {
    }
}
