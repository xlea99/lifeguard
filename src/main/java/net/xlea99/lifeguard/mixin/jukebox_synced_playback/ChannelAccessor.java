package net.xlea99.lifeguard.mixin.jukebox_synced_playback;

import com.mojang.blaze3d.audio.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** A channel's OpenAL source, to jump a song that isn't streamed to where it should start. */
@Mixin(Channel.class)
public interface ChannelAccessor {
    @Accessor("source")
    int lifeguard$source();
}
