package net.xlea99.lifeguard.mixin.zettify_all_paintings;

import me.dantaeusb.zetter.capability.canvastracker.CanvasServerTracker;
import me.dantaeusb.zetter.storage.AbstractCanvasData;
import net.xlea99.lifeguard.entry.zettify_all_paintings.VirtualPaintings;
import net.xlea99.lifeguard.entry.zettify_all_paintings.ZettifyAllPaintings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The server answers a virtual painting code with its pixel-less stub instead of looking for a file that doesn't
 * exist (breaking, pick-block, framing and copying all read it). On whenever Zetter is installed, whatever the switch:
 * a world holding virtual paintings must never hit Zetter's missing-file paths. See {@link ZettifyAllPaintings}.
 */
@Mixin(CanvasServerTracker.class)
public abstract class CanvasServerTrackerMixin {
    @Inject(method = "getCanvasData", at = @At("HEAD"), cancellable = true)
    private void lifeguard$virtualPaintingStub(String canvasCode, CallbackInfoReturnable<AbstractCanvasData> cir) {
        if (VirtualPaintings.isVirtual(canvasCode))
            cir.setReturnValue(VirtualPaintings.serverStub(((CanvasServerTracker) (Object) this).getLevel(), canvasCode));
    }
}
