package net.xlea99.lifeguard.mixin.advancement_frames_packet_checks;

import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.advframes.network.ServerBoundSetAdvancementFramePacket;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.xlea99.lifeguard.entry.advancement_frames_packet_checks.FrameChecks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** An advancement frame is only filled when it's empty, in reach, and with an advancement the player earned ({@link FrameChecks}). */
@Mixin(ServerBoundSetAdvancementFramePacket.class)
public abstract class ServerBoundSetAdvancementFramePacketMixin {
    @Shadow
    @Final
    private BlockPos pos;
    @Shadow
    @Final
    public ResourceLocation advancementId;

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private void lifeguard$checkFrame(Message.Context context, CallbackInfo ci) {
        if (!(context.getPlayer() instanceof ServerPlayer player))
            return;
        if (!FrameChecks.canReach(player, this.pos)
            || !(player.level().getBlockEntity(this.pos) instanceof AdvancementFrameBlockTile frame)
            || frame.getAdvancement() != null) {
            ci.cancel();
            return;
        }
        AdvancementHolder advancement = player.server.getAdvancements().get(this.advancementId);
        if (advancement != null && !player.getAdvancements().getOrStartProgress(advancement).isDone())
            ci.cancel();
    }
}
