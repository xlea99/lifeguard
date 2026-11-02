package net.xlea99.lifeguard.entry.zetter_easel_sync;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ZetterEaselSync {
    /**
     * None: the first stroke on a fresh canvas vanished, partly or wholly, and strokes could be lost for good. Proven
     * 2026-11-01 (zetterprobe, two canvases; bugs.md): a blank canvas is a shared default until the first stroke, so
     * that stroke races the canvas's creation (frames sent but not drawn, the client's history reset mid-stroke); the
     * server then echoes the frames the client wiped, and EaselState.processHistorySyncClient inserts actions it lacks
     * AFTER the client's current one (ListIterator.add), not in time order. Out of order, the rebuild erases the stroke
     * and performHistorySyncClient (walking back from the end, stopping at the first sent action) never sends it.
     * Three parts: the merge files every action by start time (an action it already has is just marked synced); a blank
     * canvas becomes a real one on the server right before the easel screen opens (no race left; the opener gets
     * Zetter's own init packet first); and a blank canvas that got a code but was never painted is wiped back to a
     * fresh one when it sits in an inventory, so it stacks again. Runtime-gated: the merge runs on the client, the rest
     * on the server. (Zetter 0.21.7.)
     */
    public static final Entry ENTRY = Entries.fix("zetter_easel_sync", Taste.NONE).home("zetter")
        .requiresMods("zetter")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private ZetterEaselSync() {
    }
}
