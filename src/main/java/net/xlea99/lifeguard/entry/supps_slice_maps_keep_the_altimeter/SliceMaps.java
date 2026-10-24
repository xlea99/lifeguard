package net.xlea99.lifeguard.entry.supps_slice_maps_keep_the_altimeter;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Supplementaries' slice map recipe hands the altimeter back: a map and anything in {@code #supplementaries:altimeters}
 * (Caverns & Chasms' depth gauge is one) make a slice map, and the altimeter stays in the grid instead of being used up.
 * Set where the crafting grid asks what's left after a craft (RecipeManagerMixin), so it holds for the crafting table,
 * the inventory grid and the workbenches that reuse them.
 */
public final class SliceMaps {
    private static final ResourceLocation SLICE_MAP = ResourceLocation.fromNamespaceAndPath("supplementaries", "slice_map");
    private static final TagKey<Item> ALTIMETERS =
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("supplementaries", "altimeters"));

    private SliceMaps() {
    }

    /** Keeps the altimeter in {@code remaining}, what the grid holds after the craft, when the grid makes a slice map. */
    public static void keepAltimeter(RecipeManager recipes, CraftingInput input, Level level, NonNullList<ItemStack> remaining) {
        int slot = -1;
        for (int i = 0; i < input.size() && slot < 0; i++)
            if (input.getItem(i).is(ALTIMETERS))
                slot = i;
        // Another remainder already there (some other mod's) wins.
        if (slot < 0 || slot >= remaining.size() || !remaining.get(slot).isEmpty())
            return;
        boolean sliceMap = recipes.getRecipeFor(RecipeType.CRAFTING, input, level)
            .map(recipe -> SLICE_MAP.equals(recipe.id()))
            .orElse(false);
        if (sliceMap)
            remaining.set(slot, input.getItem(slot).copyWithCount(1));
    }
}
