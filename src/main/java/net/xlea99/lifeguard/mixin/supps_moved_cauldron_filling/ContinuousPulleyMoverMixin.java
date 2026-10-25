package net.xlea99.lifeguard.mixin.supps_moved_cauldron_filling;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.mehvahdjukaar.supplementaries.common.block.cauldron.MovedFluidFiller;
import net.mehvahdjukaar.supplementaries.common.block.tiles.MovingPulleyBlockEntity;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.ContinuousPulleyMover;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.ICarryingMovingPiston;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PulleyStructureResolver;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A cauldron a pulley winds into water or lava fills with it, as one moved by hand on a rope or by a piston does. The
 * animated pulley (Supplementaries' default) never looked at the fluid it moved blocks into. Here it's noted before the
 * move clears it, the moving cauldron is filled the way Supplementaries fills a pushed one, and an Amendments cauldron
 * is handed the fluid once it lands ({@link MovingPulleyBlockEntityMixin}).
 */
@Mixin(ContinuousPulleyMover.class)
public abstract class ContinuousPulleyMoverMixin {

    @Inject(method = "moveOneStep", at = @At("HEAD"))
    private static void lifeguard$noteFluids(Level level, PulleyStructureResolver resolver, int animationTicks,
                                             CallbackInfo ci,
                                             @Share("fluids") LocalRef<Map<BlockPos, FluidState>> fluidsShare) {
        if (!CommonConfigs.Tweaks.MOVED_CAULDRON_FILLING.get())
            return;
        List<BlockPos> toPush = resolver.getToPush();
        Direction pushDir = resolver.getPushDirection();
        Map<BlockPos, FluidState> fluids = new HashMap<>();
        for (BlockPos pos : toPush) {
            BlockPos dest = pos.relative(pushDir);
            FluidState fluid = level.getFluidState(dest);
            if (!toPush.contains(dest) && !fluid.isEmpty())
                fluids.put(dest.immutable(), fluid);
        }
        fluidsShare.set(fluids);
    }

    @WrapOperation(method = "moveOneStep",
        at = @At(value = "INVOKE",
            target = "Lnet/mehvahdjukaar/supplementaries/common/block/blocks/MovingPulleyBlock;newMovingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;ZZ)Lnet/mehvahdjukaar/supplementaries/common/block/tiles/MovingPulleyBlockEntity;"))
    private static MovingPulleyBlockEntity lifeguard$fillMovedCauldron(BlockPos destPos, BlockState movingState,
                                                                       BlockState moved, Direction dir, boolean extending,
                                                                       boolean isSourcePiston,
                                                                       Operation<MovingPulleyBlockEntity> original,
                                                                       @Local(argsOnly = true) Level level,
                                                                       @Share("fluids") LocalRef<Map<BlockPos, FluidState>> fluidsShare) {
        Map<BlockPos, FluidState> fluids = fluidsShare.get();
        FluidState fluid = fluids != null ? fluids.get(destPos) : null;
        if (fluid != null)
            moved = MovedFluidFiller.fillIfMovedIntoFluid(moved, level, destPos, fluid);
        MovingPulleyBlockEntity movingBe = original.call(destPos, movingState, moved, dir, extending, isSourcePiston);
        if (fluid != null && MovedFluidFiller.needsPostPlacement(moved))
            ((ICarryingMovingPiston) movingBe).supp$setMovedFluid(fluid);
        return movingBe;
    }
}
