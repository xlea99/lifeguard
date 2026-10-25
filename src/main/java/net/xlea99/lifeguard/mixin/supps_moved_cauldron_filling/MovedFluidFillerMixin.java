package net.xlea99.lifeguard.mixin.supps_moved_cauldron_filling;

import java.util.Collections;
import net.mehvahdjukaar.supplementaries.common.block.cauldron.MovedFluidFiller;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A water or lava cauldron moved by a rope or piston only takes on water or lava when it's moved into it. Supplementaries
 * reads "moved into water and empty, or a water cauldron" (the brackets are missing), so any water cauldron came out
 * full wherever it went: pulled up a rope through air, or pushed into lava. Same for lava cauldrons.
 */
@Mixin(MovedFluidFiller.class)
public abstract class MovedFluidFillerMixin {

    @Inject(method = "fillIfMovedIntoFluid", at = @At("HEAD"), cancellable = true)
    private static void lifeguard$onlyIntoItsFluid(BlockState moved, Level level, BlockPos destPos, FluidState destFluid,
                                                   CallbackInfoReturnable<BlockState> cir) {
        if (!CommonConfigs.Tweaks.MOVED_CAULDRON_FILLING.get())
            return;
        if (moved.is(Blocks.WATER_CAULDRON))
            cir.setReturnValue(destFluid.is(Fluids.WATER)
                ? moved.setValue(LayeredCauldronBlock.LEVEL, Collections.max(LayeredCauldronBlock.LEVEL.getPossibleValues()))
                : moved);
        else if (moved.is(Blocks.LAVA_CAULDRON))
            cir.setReturnValue(moved);
    }
}
