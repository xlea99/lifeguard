package net.xlea99.lifeguard.mixin.chest_boat_chests;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.xlea99.lifeguard.entry.chest_boat_chests.BoatChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A chest boat crafted shaped (Quark's planks + chest) remembers the chest. See {@link BoatChest}. */
@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {
    @ModifyReturnValue(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
        at = @At("RETURN"))
    private ItemStack lifeguard$rememberChest(ItemStack result, CraftingInput input, HolderLookup.Provider registries) {
        return BoatChest.crafted(result, input);
    }
}
