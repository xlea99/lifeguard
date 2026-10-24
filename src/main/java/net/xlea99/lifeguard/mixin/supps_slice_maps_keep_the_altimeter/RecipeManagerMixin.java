package net.xlea99.lifeguard.mixin.supps_slice_maps_keep_the_altimeter;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.supps_slice_maps_keep_the_altimeter.SliceMaps;
import net.xlea99.lifeguard.entry.supps_slice_maps_keep_the_altimeter.SuppsSliceMapsKeepTheAltimeter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The slice map recipe hands its altimeter back ({@link SliceMaps}). */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Inject(method = "getRemainingItemsFor", at = @At("RETURN"))
    private void lifeguard$keepAltimeter(RecipeType<?> type, RecipeInput input, Level level,
                                         CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        if (type == RecipeType.CRAFTING && input instanceof CraftingInput grid && SuppsSliceMapsKeepTheAltimeter.ENTRY.active())
            SliceMaps.keepAltimeter((RecipeManager) (Object) this, grid, level, cir.getReturnValue());
    }
}
