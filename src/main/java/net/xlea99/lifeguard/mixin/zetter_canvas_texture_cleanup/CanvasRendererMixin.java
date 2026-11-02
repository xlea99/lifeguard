package net.xlea99.lifeguard.mixin.zetter_canvas_texture_cleanup;

import java.util.Map;
import me.dantaeusb.zetter.client.renderer.CanvasRenderer;
import me.dantaeusb.zetter.storage.AbstractCanvasData;
import net.xlea99.lifeguard.entry.zetter_canvas_texture_cleanup.ZetterCanvasTextureCleanup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * addCanvas closes the instance it's about to replace (Zetter only removes it from the map). Instance implements
 * AutoCloseable; it's private, so it's reached through that. See {@link ZetterCanvasTextureCleanup}.
 */
@Mixin(CanvasRenderer.class)
public abstract class CanvasRendererMixin {
    @Shadow
    @Final
    private Map<String, ? extends AutoCloseable> canvasRendererInstances;

    @Inject(method = "addCanvas", at = @At("HEAD"))
    private void lifeguard$closeReplaced(String canvasCode, AbstractCanvasData canvasData, CallbackInfo ci) {
        AutoCloseable replaced = this.canvasRendererInstances.get(canvasCode);
        if (replaced == null)
            return;
        try {
            replaced.close();
        } catch (Exception e) {
            throw new IllegalStateException("Couldn't close Zetter's canvas texture for " + canvasCode, e);
        }
    }
}
