package net.xlea99.lifeguard.mixin.zettify_all_paintings;

import me.dantaeusb.zetter.network.ServerHandler;
import me.dantaeusb.zetter.network.packet.CCanvasRequestPacket;
import net.minecraft.server.level.ServerPlayer;
import net.xlea99.lifeguard.entry.zettify_all_paintings.VirtualPaintings;
import net.xlea99.lifeguard.entry.zettify_all_paintings.ZettifyAllPaintings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A request for a virtual painting's pixels gets no answer: the server only has the blank stub, and sending it would
 * paint over the client's own image. See {@link ZettifyAllPaintings}.
 */
@Mixin(ServerHandler.class)
public abstract class ServerHandlerMixin {
    @Inject(method = "processCanvasRequest", at = @At("HEAD"), cancellable = true)
    private static void lifeguard$noPixelsForVirtualPaintings(CCanvasRequestPacket packet, ServerPlayer player, CallbackInfo ci) {
        if (VirtualPaintings.isVirtual(packet.canvasName))
            ci.cancel();
    }
}
