package net.xlea99.lifeguard.mixin.haunted_harvest_mask_limit;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.hauntedharvest.items.ModCarvedPumpkinItem;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.haunted_harvest_mask_limit.HollowPumpkins;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A worn Haunted Harvest pumpkin carved more open than a vanilla one doesn't fool endermen ({@link HollowPumpkins}). */
@Mixin(ModCarvedPumpkinItem.class)
public abstract class ModCarvedPumpkinItemMixin {

    @ModifyReturnValue(method = "isEnderMask", at = @At("RETURN"))
    private boolean lifeguard$hollowPumpkinsArentMasks(boolean mask, @Local(argsOnly = true) ItemStack stack) {
        return mask && !HollowPumpkins.tooOpen(stack);
    }
}
