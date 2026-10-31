package net.xlea99.lifeguard.entry.cc_crafted_tether_potions;

import com.teamabnormals.caverns_and_chasms.core.registry.CCItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Any potion, spinel and string, shapeless: the potion rigged up, as it is ({@link CraftedTetherPotions}). */
public final class TetherPotionRecipe extends CustomRecipe {
    public TetherPotionRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !rig(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return rig(input);
    }

    /** The rigged potion, or empty unless the grid holds exactly one potion, one spinel and one string. */
    private static ItemStack rig(CraftingInput input) {
        ItemStack potion = ItemStack.EMPTY;
        boolean spinel = false;
        boolean string = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty())
                continue;
            if (!spinel && stack.is(CCItems.SPINEL.get()))
                spinel = true;
            else if (!string && stack.is(Items.STRING))
                string = true;
            else if (potion.isEmpty() && CraftedTetherPotions.rigOf(stack.getItem()) != null)
                potion = stack;
            else
                return ItemStack.EMPTY;
        }
        return spinel && string && !potion.isEmpty() ? rigged(potion) : ItemStack.EMPTY;
    }

    /** This potion rigged up: the same potion (its whole component patch) in the matching rig. */
    public static ItemStack rigged(ItemStack potion) {
        Item rig = CraftedTetherPotions.rigOf(potion.getItem());
        return rig == null ? ItemStack.EMPTY : new ItemStack(rig.builtInRegistryHolder(), 1, potion.getComponentsPatch());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CraftedTetherPotions.SERIALIZER.get();
    }
}
