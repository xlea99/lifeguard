package net.xlea99.lifeguard.mixin.chest_minecart_chests;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.xlea99.lifeguard.entry.chest_minecart_chests.CartChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A chest minecart crafted (vanilla's minecart + any wooden chest) remembers the chest. See {@link CartChest}. */
@Mixin(ShapelessRecipe.class)
public abstract class ShapelessRecipeMixin {
    @ModifyReturnValue(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
        at = @At("RETURN"))
    private ItemStack lifeguard$rememberCartChest(ItemStack result, CraftingInput input, HolderLookup.Provider registries) {
        return CartChest.crafted(result, input);
    }
}
