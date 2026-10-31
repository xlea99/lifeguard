package net.xlea99.lifeguard.mixin.jukebox_playlists;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.jukebox_playlists.Playlists;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** A song played to its end (the only stop {@code tick} makes) cues the jukebox's next track ({@link Playlists}). */
@Mixin(JukeboxSongPlayer.class)
public abstract class JukeboxSongPlayerMixin {
    @Shadow
    @Final
    private BlockPos blockPos;

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/JukeboxSongPlayer;stop(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void lifeguard$songEnded(JukeboxSongPlayer self, LevelAccessor level, BlockState state, Operation<Void> original) {
        original.call(self, level, state);
        if (level.getBlockEntity(this.blockPos) instanceof JukeboxBlockEntity jukebox)
            Playlists.songEnded(jukebox);
    }
}
