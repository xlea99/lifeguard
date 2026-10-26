package net.xlea99.lifeguard.mixin.advancement_frames_packet_checks;

import net.mehvahdjukaar.advframes.blocks.StatFrameBlockTile;
import net.mehvahdjukaar.advframes.network.ServerBoundSetStatFramePacket;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.xlea99.lifeguard.entry.advancement_frames_packet_checks.FrameChecks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A stat frame is only filled when it's empty and in reach ({@link FrameChecks}). */
@Mixin(ServerBoundSetStatFramePacket.class)
public abstract class ServerBoundSetStatFramePacketMixin {
    @Shadow
    @Final
    private BlockPos pos;

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private void lifeguard$checkFrame(Message.Context context, CallbackInfo ci) {
        if (!(context.getPlayer() instanceof ServerPlayer player))
            return;
        if (!FrameChecks.canReach(player, this.pos)
            || !(player.level().getBlockEntity(this.pos) instanceof StatFrameBlockTile frame)
            || frame.getStat() != null)
            ci.cancel();
    }
}
