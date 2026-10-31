package net.xlea99.lifeguard.mixin.jukebox_synced_playback;

import java.util.Map;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The songs each jukebox is playing here, by its position. */
@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
    @Accessor("playingJukeboxSongs")
    Map<BlockPos, SoundInstance> lifeguard$playingJukeboxSongs();
}
