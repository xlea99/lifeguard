package net.xlea99.lifeguard.mixin.amendments_lava_layers;

import net.mehvahdjukaar.amendments.events.behaviors.CauldronConversion;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.amendments_lava_layers.LavaLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Amendments picks the block for a cauldron by what it holds, and for exactly a bucket of lava it picks vanilla's lava
 * cauldron. With lava kept as layers ({@link LavaLayers}) any amount of lava stays an Amendments cauldron (lava never
 * boils, so the default state is the right one).
 */
@Mixin(CauldronConversion.class)
public abstract class CauldronConversionMixin {

    @Inject(method = "getNewState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/mehvahdjukaar/moonlight/api/fluids/SoftFluidStack;)Lnet/minecraft/world/level/block/state/BlockState;",
        at = @At("HEAD"), cancellable = true)
    private static void lifeguard$keepLavaLayered(BlockPos pos, Level level, SoftFluidStack fluid,
                                                  CallbackInfoReturnable<BlockState> cir) {
        if (!fluid.isEmpty() && fluid.is(MLBuiltinSoftFluids.LAVA) && LavaLayers.cauldronsOn())
            cir.setReturnValue(ModRegistry.LIQUID_CAULDRON.get().defaultBlockState());
    }
}
