package net.xlea99.lifeguard.mixin.clayworks_flower_pots;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.ClayworksFlowerPots;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.FlowerPots;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** The pottery table's pot slot takes a flower pot too. */
@Mixin(targets = "com.teamabnormals.clayworks.common.inventory.PotteryMenu$3")
public abstract class PotSlotMixin {

    @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
    private boolean lifeguard$flowerPots(boolean decoratedPot, @Local(argsOnly = true) ItemStack stack) {
        return decoratedPot || ClayworksFlowerPots.ENTRY.active() && FlowerPots.isFlowerPot(stack);
    }
}
