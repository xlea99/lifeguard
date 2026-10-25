package net.xlea99.lifeguard.mixin.cauldron_fourths.amendments;

import net.mehvahdjukaar.amendments.events.behaviors.CauldronConversion;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Amendments picks a cauldron for a liquid by its amount: water goes into a water cauldron of at most 3 levels, and a
 * whole bucket of powder snow into a powder snow cauldron at its default (1 level). With four levels, water fills up to
 * 4, and any amount of powder snow becomes a powder snow cauldron of that many levels.
 */
@Mixin(CauldronConversion.class)
public abstract class CauldronConversionMixin {

    @ModifyConstant(method = "getNewState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/mehvahdjukaar/moonlight/api/fluids/SoftFluidStack;)Lnet/minecraft/world/level/block/state/BlockState;",
        constant = @Constant(intValue = 3))
    private static int lifeguard$waterUpToFour(int full) {
        return 4;
    }

    @Inject(method = "getNewState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/mehvahdjukaar/moonlight/api/fluids/SoftFluidStack;)Lnet/minecraft/world/level/block/state/BlockState;",
        at = @At("HEAD"), cancellable = true)
    private static void lifeguard$powderSnowByLevel(BlockPos pos, Level level, SoftFluidStack fluid,
                                                    CallbackInfoReturnable<BlockState> cir) {
        if (!fluid.isEmpty() && fluid.is(MLBuiltinSoftFluids.POWDERED_SNOW))
            cir.setReturnValue(Blocks.POWDER_SNOW_CAULDRON.defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, Math.min(4, fluid.getCount())));
    }
}
