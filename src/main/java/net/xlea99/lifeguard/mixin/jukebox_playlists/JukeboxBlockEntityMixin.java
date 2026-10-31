package net.xlea99.lifeguard.mixin.jukebox_playlists;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.jukebox_playlists.PlaylistJukebox;
import net.xlea99.lifeguard.entry.jukebox_playlists.Playlists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A jukebox plays its playlist ({@link Playlists}): when a song finishes, the next one comes in after a short silence. A
 * jukebox with a disc in it ticks, so the wait is counted here; it and the playlist's place are saved with the jukebox.
 */
@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin implements PlaylistJukebox {
    @Unique
    private int lifeguard$nextTrackIn = -1;
    @Unique
    private int lifeguard$cursor;
    @Unique
    private final List<String> lifeguard$played = new ArrayList<>();

    @Inject(method = "tick", at = @At("HEAD"))
    private static void lifeguard$countDown(Level level, BlockPos pos, BlockState state, JukeboxBlockEntity jukebox,
                                            CallbackInfo ci) {
        if (((PlaylistJukebox) jukebox).lifeguard$trackDue())
            Playlists.nextTrack(jukebox);
    }

    @Override
    public boolean lifeguard$trackDue() {
        // Something already started a song (a disc put in by hand or by a hopper while it waited).
        if (this.lifeguard$nextTrackIn > 0 && ((JukeboxBlockEntity) (Object) this).getSongPlayer().isPlaying())
            this.lifeguard$nextTrackIn = -1;
        if (this.lifeguard$nextTrackIn > 0 && --this.lifeguard$nextTrackIn == 0) {
            this.lifeguard$nextTrackIn = -1;
            return true;
        }
        return false;
    }

    @Override
    public void lifeguard$waitForNextTrack(int ticks) {
        this.lifeguard$nextTrackIn = ticks;
    }

    @Override
    public void lifeguard$cancelNextTrack() {
        this.lifeguard$nextTrackIn = -1;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void lifeguard$savePlaylist(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this.lifeguard$nextTrackIn > 0)
            tag.putInt("lifeguard_next_track_in", this.lifeguard$nextTrackIn);
        if (this.lifeguard$cursor != 0)
            tag.putInt("lifeguard_cursor", this.lifeguard$cursor);
        if (!this.lifeguard$played.isEmpty()) {
            ListTag played = new ListTag();
            this.lifeguard$played.forEach(id -> played.add(StringTag.valueOf(id)));
            tag.put("lifeguard_played", played);
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void lifeguard$loadPlaylist(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.lifeguard$nextTrackIn = tag.contains("lifeguard_next_track_in") ? tag.getInt("lifeguard_next_track_in") : -1;
        this.lifeguard$cursor = tag.getInt("lifeguard_cursor");
        this.lifeguard$played.clear();
        tag.getList("lifeguard_played", Tag.TAG_STRING).forEach(id -> this.lifeguard$played.add(id.getAsString()));
    }

    @Override
    public int lifeguard$cursor() {
        return this.lifeguard$cursor;
    }

    @Override
    public void lifeguard$setCursor(int slot) {
        this.lifeguard$cursor = slot;
    }

    @Override
    public List<String> lifeguard$played() {
        return this.lifeguard$played;
    }
}
