package net.xlea99.lifeguard.entry.quark_chest_minecart_recipe;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class QuarkChestMinecartRecipe {
    /**
     * None: Quark's Variant Chests turns on its built-in datapack {@code quark_vdo_variant_chests}, whose pack.mcmeta
     * filter blocks {@code minecraft:recipe/chest} to hide vanilla's chest recipe. Filter paths are regexes matched
     * anywhere ({@code Pattern.asPredicate}, a find), so it also hid {@code recipe/chest_minecart.json}: vanilla's minecart
     * + chest recipe never loaded (proven 2026-10-31 by cartprobe: no pack supplied the file, the control
     * oak_chest_boat did). Back as the same recipe under our own namespace, which the filter can't reach, only while
     * Variant Chests is on (data/lifeguard/recipe/quark_chest_minecart_recipe/). (Quark 4.1-488.)
     */
    public static final Entry ENTRY = Entries.fix("quark_chest_minecart_recipe", Taste.NONE).home("quark")
        .requiresMods("quark")
        .side(Side.SERVER).restart(Restart.WORLD);

    private QuarkChestMinecartRecipe() {
    }
}
