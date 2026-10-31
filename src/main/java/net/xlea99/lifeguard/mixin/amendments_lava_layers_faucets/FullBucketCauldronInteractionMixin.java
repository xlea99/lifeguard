package net.xlea99.lifeguard.mixin.amendments_lava_layers_faucets;

import net.mehvahdjukaar.moonlight.api.fluids.FluidOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.amendments_lava_layers.LavaLayers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A faucet pours lava into an empty cauldron a layer at a time, like any other liquid ({@link LavaLayers}). Faucets
 * try their targets in order, and Supplementaries' own one for vanilla's lava cauldron comes before Amendments' layered
 * one: it only ever pours a whole bucket, making a lava cauldron (which then turns into four layers). With lava kept as
 * layers it steps aside, so Amendments' target pours what the source offers. A lava source block still gives its whole
 * bucket at once: taking any of it takes the block.
 */
@Mixin(targets = "net.mehvahdjukaar.supplementaries.common.block.faucet.FullBucketCauldronInteraction")
public abstract class FullBucketCauldronInteractionMixin {

    @Shadow
    @Final
    private BlockState fullCauldron;

    @Inject(method = "fill", at = @At("HEAD"), cancellable = true)
    private void lifeguard$lavaByLayers(Level level, BlockPos pos, BlockState state, FluidOffer offer,
                                        CallbackInfoReturnable<Integer> cir) {
        if (this.fullCauldron.is(Blocks.LAVA_CAULDRON) && LavaLayers.cauldronsOn())
            cir.setReturnValue(null);
    }
}
