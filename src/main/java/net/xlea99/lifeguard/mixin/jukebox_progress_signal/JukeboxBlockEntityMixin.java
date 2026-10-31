package net.xlea99.lifeguard.mixin.jukebox_progress_signal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.jukebox_progress_signal.JukeboxProgress;
import net.xlea99.lifeguard.entry.jukebox_progress_signal.ProgressJukebox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Each tick a jukebox checks whether its progress reading moved on ({@link JukeboxProgress}), and tells comparators when
 * it has (about 15 times a song).
 */
@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin implements ProgressJukebox {
    @Unique
    private int lifeguard$lastProgress = -1;

    @Inject(method = "tick", at = @At("TAIL"))
    private static void lifeguard$progressMoved(Level level, BlockPos pos, BlockState state, JukeboxBlockEntity jukebox,
                                                CallbackInfo ci) {
        // Jukeboxes tick on both sides; comparators only read the server's.
        if (level.isClientSide)
            return;
        ProgressJukebox progressJukebox = (ProgressJukebox) jukebox;
        int progress = JukeboxProgress.of(jukebox);
        if (progress != progressJukebox.lifeguard$lastProgress()) {
            progressJukebox.lifeguard$setLastProgress(progress);
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }

    @Override
    public int lifeguard$lastProgress() {
        return this.lifeguard$lastProgress;
    }

    @Override
    public void lifeguard$setLastProgress(int progress) {
        this.lifeguard$lastProgress = progress;
    }
}
