package net.xlea99.lifeguard.mixin.jukebox_progress_signal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.jukebox_progress_signal.JukeboxProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A comparator reads how far through its song the jukebox is ({@link JukeboxProgress}), not the song's number. */
@Mixin(JukeboxBlock.class)
public abstract class JukeboxBlockMixin {
    @Inject(method = "getAnalogOutputSignal", at = @At("HEAD"), cancellable = true)
    private void lifeguard$songProgress(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox)
            cir.setReturnValue(JukeboxProgress.of(jukebox));
    }
}
