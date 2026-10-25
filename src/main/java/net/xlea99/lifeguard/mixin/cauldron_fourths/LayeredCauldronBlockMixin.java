package net.xlea99.lifeguard.mixin.cauldron_fourths;

import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A layered cauldron is full at four levels, rain and snow fill it to four, and its liquid sits at 9, 11, 13 and 15
 * pixels: the heights Amendments' own four-level cauldron uses, so the same fill looks the same in either.
 */
@Mixin(LayeredCauldronBlock.class)
public abstract class LayeredCauldronBlockMixin {

    @ModifyConstant(method = {"isFull", "handlePrecipitation"}, constant = @Constant(intValue = 3))
    private int lifeguard$fullAtFour(int full) {
        return 4;
    }

    @Inject(method = "getContentHeight", at = @At("HEAD"), cancellable = true)
    private void lifeguard$fourthHeights(BlockState state, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue((7.0 + state.getValue(LayeredCauldronBlock.LEVEL) * 2.0) / 16.0);
    }
}
