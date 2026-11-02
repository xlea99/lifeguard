package net.xlea99.lifeguard.entry.zetter_painting_lighting;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ZetterPaintingLighting {
    /**
     * None: a 1-tall Zetter painting under an overhang drew pitch black. Zetter lights the canvas (and a 1x1's frame) with
     * the entity's packed light, read at its light probe = its EYE position. Its 1.20.1 getEyeHeight override (0) is
     * dead in 1.21, so the eye sits 0.85 above the center: for a 1-tall painting that's the block above it, in front of
     * the wall (an overhang = light 0); taller paintings' probes land in their own top row. Proven 2026-11-01 (user, all
     * three predictions: glass in place of the overhang lights it at once; a 1x2 under the same overhang stays lit; a
     * 2x1's canvas goes dark while its per-block-lit frame stays lit). Now the type has eyeHeight(0), as vanilla 1.21
     * gives item frames for the same reason. (Zetter 0.21.7, ZetterEntities / PaintingEntity.getEyeHeight.)
     */
    public static final Entry ENTRY = Entries.fix("zetter_painting_lighting", Taste.NONE).home("zetter")
        .requiresMods("zetter")
        .side(Side.CLIENT).restart(Restart.GAME);

    private ZetterPaintingLighting() {
    }
}
