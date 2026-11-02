package net.xlea99.lifeguard.entry.zetter_missing_painting_drops;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ZetterMissingPaintingDrops {
    /**
     * None: breaking a hung Zetter painting whose picture data is gone crashed the server. PaintingEntity.dropItem passes
     * the canvas tracker's answer straight into storePaintingData, which reads its author: null for a deleted painting
     * file, and for a zettified painting whose painting_variant no longer exists (its mod removed, a datapack changed;
     * zettify_all_paintings' stub has nothing to answer). Zetter's own getPickResult null-checks the same lookup. Now the
     * drop keeps what the entity still knows (code, size, generation), so nothing is lost: the painting works again if
     * its data or variant comes back. Found by reading the code (audit U4, 2026-11-01); the crash is a plain null
     * dereference. (Zetter 0.21.7, PaintingEntity.dropItem.)
     */
    public static final Entry ENTRY = Entries.fix("zetter_missing_painting_drops", Taste.NONE).home("zetter")
        .requiresMods("zetter")
        .side(Side.SERVER).restart(Restart.GAME);

    private ZetterMissingPaintingDrops() {
    }
}
