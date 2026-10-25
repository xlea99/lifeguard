package net.xlea99.lifeguard.mixin.supps_continuous_hand_ropes;

import net.mehvahdjukaar.supplementaries.common.block.blocks.AbstractRopeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.xlea99.lifeguard.entry.supps_continuous_hand_ropes.HandRopes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Runs a hand rope's queued move ({@link HandRopes}) on the server and on every client watching. */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Inject(method = "triggerEvent", at = @At("HEAD"), cancellable = true)
    private void lifeguard$handRopeMove(Level level, BlockPos pos, int id, int param, CallbackInfoReturnable<Boolean> cir) {
        if (id != HandRopes.EVENT)
            return;
        Block block = ((BlockBehaviour.BlockStateBase) (Object) this).getBlock();
        if (block instanceof AbstractRopeBlock)
            cir.setReturnValue(HandRopes.onEvent(level, pos, block, param));
    }
}
