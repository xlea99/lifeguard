package net.xlea99.lifeguard.mixin.supps_jukebox_speakers;

import net.mehvahdjukaar.supplementaries.common.block.blocks.SpeakerBlock;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.supps_jukebox_speakers.JukeboxSpeakers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A jukebox's speakers puff in one color together ({@link JukeboxSpeakers}): the jukebox picks it and sends it along
 * (as 1 + the color), where the speaker's own puff picks one at random.
 */
@Mixin(SpeakerBlock.class)
public abstract class SpeakerBlockMixin {
    @Inject(method = "triggerEvent", at = @At("HEAD"), cancellable = true)
    private void lifeguard$jukeboxPuff(BlockState state, Level level, BlockPos pos, int event, int param,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (event != 0 || param <= 0)
            return;
        Direction facing = state.getValue(SpeakerBlock.FACING);
        level.addParticle(ModParticles.SPEAKER_SOUND.get(), pos.getX() + 0.5 + facing.getStepX() * 0.725, pos.getY() + 0.5,
            pos.getZ() + 0.5 + facing.getStepZ() * 0.725, (param - 1) / 24.0, 0, 0);
        cir.setReturnValue(true);
    }
}
