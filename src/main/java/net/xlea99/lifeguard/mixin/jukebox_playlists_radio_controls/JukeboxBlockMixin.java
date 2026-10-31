package net.xlea99.lifeguard.mixin.jukebox_playlists_radio_controls;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.xlea99.lifeguard.entry.jukebox_playlists.Playlists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A jukebox with a playlist touching it is a radio ({@link Playlists#use}): right-click skips, sneak-right-click turns
 * it off, right-clicking it empty turns it on. Vanilla would eject the disc instead. A click holding something reaches
 * here too once the jukebox has passed on taking it.
 */
@Mixin(JukeboxBlock.class)
public abstract class JukeboxBlockMixin {
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void lifeguard$radioButtons(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit,
                                        CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = Playlists.use(state, level, pos, player);
        if (result != null)
            cir.setReturnValue(result);
    }
}
