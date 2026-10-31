package net.xlea99.lifeguard.mixin.jukebox_synced_playback;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.jukebox_synced_playback.JukeboxSync;
import net.xlea99.lifeguard.entry.jukebox_synced_playback.JukeboxSyncedPlayback;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * While a jukebox plays, it tells everyone in earshot, once a second, where it is in its song ({@link JukeboxSync}), and
 * when it stops, everyone it was carried to.
 */
@Mixin(JukeboxSongPlayer.class)
public abstract class JukeboxSongPlayerMixin {
    @Shadow
    @Nullable
    private Holder<JukeboxSong> song;
    @Shadow
    private long ticksSinceSongStarted;
    @Shadow
    @Final
    private BlockPos blockPos;

    /** How far it was last heard, for its stop to reach everyone its music did. */
    @Unique
    private int lifeguard$range = JukeboxSync.RANGE;

    @Inject(method = "tick", at = @At("TAIL"))
    private void lifeguard$heartbeat(LevelAccessor level, @Nullable BlockState state, CallbackInfo ci) {
        if (this.song == null || !(level instanceof ServerLevel server) || !JukeboxSyncedPlayback.ENTRY.active())
            return;
        if (this.ticksSinceSongStarted > 0 && this.ticksSinceSongStarted % JukeboxSync.HEARTBEAT_TICKS == 0)
            this.lifeguard$range = JukeboxSync.heartbeat(server, this.blockPos, this.song, this.ticksSinceSongStarted,
                this.lifeguard$range);
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void lifeguard$stopFarAway(LevelAccessor level, @Nullable BlockState state, CallbackInfo ci) {
        if (this.song != null && level instanceof ServerLevel server && JukeboxSyncedPlayback.ENTRY.active())
            JukeboxSync.stop(server, this.blockPos, this.lifeguard$range);
        this.lifeguard$range = JukeboxSync.RANGE;
    }
}
