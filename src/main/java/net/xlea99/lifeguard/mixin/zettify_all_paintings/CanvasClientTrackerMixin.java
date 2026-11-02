package net.xlea99.lifeguard.mixin.zettify_all_paintings;

import me.dantaeusb.zetter.capability.canvastracker.CanvasClientTracker;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.zettify_all_paintings.ClientVirtualPaintings;
import net.xlea99.lifeguard.entry.zettify_all_paintings.ZettifyAllPaintings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Zetter's client tracker gets its level: the virtual paintings join it. See {@link ZettifyAllPaintings}. */
@Mixin(CanvasClientTracker.class)
public abstract class CanvasClientTrackerMixin {
    @Inject(method = "setLevel", at = @At("TAIL"))
    private void lifeguard$registerVirtualPaintings(Level level, CallbackInfo ci) {
        ClientVirtualPaintings.registerAll((CanvasClientTracker) (Object) this, level);
    }
}
