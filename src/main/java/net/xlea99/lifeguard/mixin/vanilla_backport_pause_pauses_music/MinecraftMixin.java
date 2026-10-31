package net.xlea99.lifeguard.mixin.vanilla_backport_pause_pauses_music;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla Backport swaps pauseGame's "pause every sound" for "pause every sound but the music" (1.21.6's music on the
 * pause screen, no config). This pauses the music too, once the game has paused; unpausing already resumes every sound.
 * Done after pauseGame rather than on its pause call, which Vanilla Backport wraps.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "pauseGame", at = @At("RETURN"))
    private void lifeguard$pauseMusicToo(boolean pauseOnly, CallbackInfo ci) {
        Minecraft minecraft = (Minecraft) (Object) this;
        IntegratedServer server = minecraft.getSingleplayerServer();
        // The same test pauseGame pauses by: a singleplayer world, not opened to LAN.
        if (server != null && !server.isPublished() && minecraft.screen instanceof PauseScreen)
            minecraft.getSoundManager().pause();
    }
}
