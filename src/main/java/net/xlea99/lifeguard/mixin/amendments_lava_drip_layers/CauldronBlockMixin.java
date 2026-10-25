package net.xlea99.lifeguard.mixin.amendments_lava_drip_layers;

import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dripstone dripping lava into an empty cauldron adds one layer when Amendments' consistent_lava_layers is on, as it
 * already does for a cauldron that holds some lava. Amendments swaps the empty cauldron for one of its own and adds the
 * layer, but doesn't stop vanilla's drip right after, which replaces it with a full lava cauldron: a whole bucket. Here
 * vanilla's drip is stopped once the swap is made (making the swap first if Amendments' own step hasn't run yet).
 */
@Mixin(CauldronBlock.class)
public abstract class CauldronBlockMixin {

    @Inject(method = "receiveStalactiteDrip", at = @At("HEAD"), cancellable = true)
    private void lifeguard$oneLayerOfLava(BlockState state, Level level, BlockPos pos, Fluid fluid, CallbackInfo ci) {
        if (fluid != Fluids.LAVA || !CommonConfigs.LAVA_LAYERS.get() || !CommonConfigs.LIQUID_CAULDRON.get())
            return;
        if (level.getBlockState(pos).is(Blocks.CAULDRON)) {
            LiquidCauldronBlock block = ModRegistry.LIQUID_CAULDRON.get();
            level.setBlockAndUpdate(pos, block.defaultBlockState());
            block.receiveStalactiteDrip(block.defaultBlockState(), level, pos, fluid);
        }
        ci.cancel();
    }
}
