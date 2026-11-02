package net.xlea99.lifeguard.entry.zetter_tooltips;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ZetterTooltips {
    /**
     * None: Zetter's item tooltips never showed. A painting's "by <author>" and "Original, 2x2" lines and a canvas's size
     * live in appendHoverText(ItemStack, Level, List, TooltipFlag), the 1.20.1 signature; 1.21.1 calls
     * appendHoverText(ItemStack, Item.TooltipContext, List, TooltipFlag), so Zetter's overrides nothing and never runs
     * (javap on the jar, 2026-11-01; user: no author on any painting). Now, when Item's default runs for a Zetter canvas,
     * painting or frame, it calls Zetter's own with the context's level; if Zetter ever fixes its signature, the default
     * never runs for its items and this goes idle. (Zetter 0.21.7, CanvasItem / PaintingItem.appendHoverText.)
     */
    public static final Entry ENTRY = Entries.fix("zetter_tooltips", Taste.NONE).home("zetter")
        .requiresMods("zetter")
        .side(Side.CLIENT).restart(Restart.GAME);

    private ZetterTooltips() {
    }
}
