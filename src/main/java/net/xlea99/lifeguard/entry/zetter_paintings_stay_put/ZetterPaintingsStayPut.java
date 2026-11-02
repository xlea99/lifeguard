package net.xlea99.lifeguard.entry.zetter_paintings_stay_put;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ZetterPaintingsStayPut {
    /**
     * None: a Zetter painting with an even height (2x2, 1x2, 3x2, 4x4) hopped one block up on every client a moment
     * after it was hung. The server tracks Zetter's painting by its center, which for an even height sits exactly on a
     * block edge (y 69.0), and sends it in the routine position updates; the client applied it through Entity's default
     * lerpTo, which floors the edge into the block above. Zetter has its own lerpTo for this (move the anchor by the
     * difference, so an unchanged center changes nothing), but it kept the 1.20.1 signature (a trailing boolean), which
     * overrides nothing in 1.21.1, so it never ran. Proven 2026-11-01 (zframeprobe, control: a vanilla 2x2 painting
     * takes the same packet through Painting's own lerpTo and stays put). The fix calls Zetter's own lerpTo in place of
     * the default; if Zetter ever fixes its signature, the default never runs for its paintings and this goes idle.
     * (Zetter 0.21.7, PaintingEntity.lerpTo.)
     */
    public static final Entry ENTRY = Entries.fix("zetter_paintings_stay_put", Taste.NONE).home("zetter")
        .requiresMods("zetter")
        .side(Side.CLIENT).restart(Restart.GAME);

    private ZetterPaintingsStayPut() {
    }
}
