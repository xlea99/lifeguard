package net.xlea99.lifeguard.mixin.supps_moved_cauldron_filling;

import net.mehvahdjukaar.supplementaries.common.block.tiles.MovingPulleyBlockEntity;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.ICarryingMovingPiston;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Once a block a pulley moved lands (with its contents back), it's handed the fluid it was wound into, as Supplementaries
 * does for pistons: how an Amendments cauldron fills (see {@link ContinuousPulleyMoverMixin}). A pulley's moving block
 * is removed only when it lands; the fluid is only ever noted on the server.
 */
@Mixin(MovingPulleyBlockEntity.class)
public abstract class MovingPulleyBlockEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private static void lifeguard$fillOnLanding(Level level, BlockPos pos, BlockState state, MovingPulleyBlockEntity be,
                                                CallbackInfo ci) {
        if (be.isRemoved())
            ((ICarryingMovingPiston) be).supp$applyMovedFluid();
    }
}
