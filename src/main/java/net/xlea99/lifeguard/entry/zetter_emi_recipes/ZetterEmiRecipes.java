package net.xlea99.lifeguard.entry.zetter_emi_recipes;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ZetterEmiRecipes {
    /**
     * None: Zetter's four special crafting recipes (framing, unframing, copying, palette recharge) are custom recipe types
     * that work out their result from the grid, so EMI couldn't show them (user-confirmed 2026-11-01): nobody could find
     * out how to frame or copy a painting. Now EMI lists them with example items, one framing and unframing recipe per
     * frame. Display only. (Zetter 0.21.7.)
     */
    public static final Entry ENTRY = Entries.compat("zetter_emi_recipes", Taste.NONE).home("zetter")
        .requiresMods("zetter", "emi")
        .side(Side.CLIENT).restart(Restart.GAME);

    private ZetterEmiRecipes() {
    }
}
