package net.xlea99.lifeguard.mixin.quark_elytra_duplication_crash;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.crafting.CraftingInput;
import net.xlea99.lifeguard.entry.quark_elytra_duplication_crash.QuarkElytraDuplicationCrash;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.violetmoon.quark.content.tweaks.recipe.ElytraDuplicationRecipe;

/**
 * Quark 4.1 builds the recipe's leftovers list with one slot per ingredient (2), but vanilla's result slot reads it with
 * one slot per crafting grid slot. The grid is trimmed to the items' bounding box, so any diagonal layout is a 2x2 box:
 * vanilla asks for slot 3 of a 2-slot list and the game crashes. Side by side is a 1x2 box, which is why it usually
 * works.
 */
@Mixin(ElytraDuplicationRecipe.class)
public abstract class ElytraDuplicationRecipeMixin {

    @ModifyArg(method = "getRemainingItems",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/NonNullList;withSize(ILjava/lang/Object;)Lnet/minecraft/core/NonNullList;"),
        index = 0)
    private int lifeguard$oneLeftoverPerGridSlot(int size, @Local(argsOnly = true) CraftingInput input) {
        return QuarkElytraDuplicationCrash.ENTRY.active() ? input.size() : size;
    }
}
