package net.xlea99.lifeguard.mixin.ac_infinite_soda.faucets;

import net.mehvahdjukaar.moonlight.api.fluids.FluidOffer;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.xlea99.lifeguard.entry.ac_infinite_soda.InfiniteSoda;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A faucet on a purple soda source pours it like water, now that soda is infinite. Supplementaries treats only water as
 * endless here: water pours as little as a bottle at a time and never drains its source, while any other liquid only
 * pours where a whole bucket fits, and takes the source block with it.
 */
@Mixin(targets = "net.mehvahdjukaar.supplementaries.common.block.faucet.LiquidBlockInteraction")
public abstract class LiquidBlockInteractionMixin {

    @Inject(method = "getProvidedFluid", at = @At("HEAD"), cancellable = true)
    private void lifeguard$sodaLikeWater(Level level, BlockPos pos, Direction dir, FluidState fluid,
                                         CallbackInfoReturnable<FluidOffer> cir) {
        if (fluid.isSource() && InfiniteSoda.isSoda(fluid))
            cir.setReturnValue(FluidOffer.of(
                SoftFluidStack.fromFluid(fluid.getType(), SoftFluid.BUCKET_COUNT, level.registryAccess()), 1));
    }

    @Inject(method = "drain", at = @At("HEAD"), cancellable = true)
    private void lifeguard$keepSodaSource(Level level, BlockPos pos, Direction dir, FluidState fluid, int amount,
                                          CallbackInfo ci) {
        if (InfiniteSoda.isSoda(fluid))
            ci.cancel();
    }
}
