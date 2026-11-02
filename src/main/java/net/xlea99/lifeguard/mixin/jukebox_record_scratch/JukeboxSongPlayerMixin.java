package net.xlea99.lifeguard.mixin.jukebox_record_scratch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.jukebox_record_scratch.RecordScratch;
import net.xlea99.lifeguard.entry.jukebox_record_scratch.ScratchedJukebox;
import net.xlea99.lifeguard.entry.jukebox_synced_playback.JukeboxSync;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A scratched song holds its place: its clock (and so its heartbeat, redstone and ending) waits while a creeper by it is
 * still hissing and through the awkward silence after, then it carries on, and everyone in earshot is told at once so
 * the music comes back mid-song.
 */
@Mixin(JukeboxSongPlayer.class)
public abstract class JukeboxSongPlayerMixin implements ScratchedJukebox {
    @Shadow
    @Nullable
    private Holder<JukeboxSong> song;
    @Shadow
    private long ticksSinceSongStarted;
    @Shadow
    @Final
    private BlockPos blockPos;

    @Unique
    private boolean lifeguard$scratched;
    /** Ticks of awkward silence left once nothing by it is hissing. */
    @Unique
    private int lifeguard$silence;

    @Override
    public boolean lifeguard$scratch() {
        if (this.lifeguard$scratched)
            return false;
        this.lifeguard$scratched = true;
        this.lifeguard$silence = RecordScratch.AWKWARD_SILENCE;
        return true;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void lifeguard$holdThePlace(LevelAccessor level, @Nullable BlockState state, CallbackInfo ci) {
        if (!this.lifeguard$scratched)
            return;
        if (this.song == null || !(level instanceof ServerLevel server)) {
            this.lifeguard$scratched = false;
            return;
        }
        if (RecordScratch.stillHissing(server, this.blockPos)) {
            this.lifeguard$silence = RecordScratch.AWKWARD_SILENCE;
            ci.cancel();
            return;
        }
        if (this.lifeguard$silence > 0) {
            this.lifeguard$silence--;
            ci.cancel();
            return;
        }
        this.lifeguard$scratched = false;
        JukeboxSync.heartbeat(server, this.blockPos, this.song, this.ticksSinceSongStarted, JukeboxSync.RANGE);
    }

    @Inject(method = {"play", "stop"}, at = @At("HEAD"))
    private void lifeguard$forgetTheScratch(CallbackInfo ci) {
        this.lifeguard$scratched = false;
    }
}
