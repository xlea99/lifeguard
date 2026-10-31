package net.xlea99.lifeguard.entry.species_dyeable_chaindeliers;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** A Chaindelier and any dyes: the Chaindelier with them mixed into its glow ({@link Chaindeliers}). */
public class ChaindelierDyeRecipe extends CustomRecipe {
    public ChaindelierDyeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !this.assemble(input, level.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack chaindelier = ItemStack.EMPTY;
        List<DyeItem> dyes = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty())
                continue;
            if (Chaindeliers.is(stack)) {
                if (!chaindelier.isEmpty())
                    return ItemStack.EMPTY;
                chaindelier = stack;
            } else if (stack.getItem() instanceof DyeItem dye)
                dyes.add(dye);
            else
                return ItemStack.EMPTY;
        }
        if (chaindelier.isEmpty() || dyes.isEmpty())
            return ItemStack.EMPTY;
        ItemStack dyed = chaindelier.copyWithCount(1);
        Chaindeliers.setColor(dyed, Chaindeliers.mix(Chaindeliers.colorOf(chaindelier), dyes));
        return dyed;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Chaindeliers.DYEING.get();
    }
}
