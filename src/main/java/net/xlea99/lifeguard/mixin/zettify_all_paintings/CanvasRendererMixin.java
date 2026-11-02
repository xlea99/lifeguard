package net.xlea99.lifeguard.mixin.zettify_all_paintings;

import me.dantaeusb.zetter.client.renderer.CanvasRenderer;
import net.xlea99.lifeguard.entry.zettify_all_paintings.VirtualPaintings;
import net.xlea99.lifeguard.entry.zettify_all_paintings.ZettifyAllPaintings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The client never asks the server for a virtual painting's pixels (one this client couldn't build, e.g. animated or
 * its texture missing, would otherwise be re-requested on a loop). See {@link ZettifyAllPaintings}.
 */
@Mixin(CanvasRenderer.class)
public abstract class CanvasRendererMixin {
    @Inject(method = "queueCanvasTextureUpdate", at = @At("HEAD"), cancellable = true)
    private void lifeguard$neverRequestVirtualPaintings(String canvasCode, CallbackInfo ci) {
        if (VirtualPaintings.isVirtual(canvasCode))
            ci.cancel();
    }
}
