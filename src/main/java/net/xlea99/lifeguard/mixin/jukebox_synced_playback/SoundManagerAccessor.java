package net.xlea99.lifeguard.mixin.jukebox_synced_playback;

import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SoundManager.class)
public interface SoundManagerAccessor {
    @Accessor("soundEngine")
    SoundEngine lifeguard$soundEngine();
}
