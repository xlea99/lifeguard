package net.xlea99.lifeguard.mixin.zetter_canvas_timers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.dantaeusb.zetter.client.renderer.CanvasRenderer;
import me.dantaeusb.zetter.core.ZetterClientModEvents;
import net.xlea99.lifeguard.entry.zetter_canvas_timers.ZetterCanvasTimers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Zetter's canvas timers get ticks, not milliseconds. See {@link ZetterCanvasTimers}. */
@Mixin(ZetterClientModEvents.class)
public abstract class ZetterClientModEventsMixin {
    @WrapOperation(method = "onRenderTickStart", at = @At(value = "INVOKE",
        target = "Lme/dantaeusb/zetter/client/renderer/CanvasRenderer;update(J)V"))
    private static void lifeguard$ticksNotMillis(CanvasRenderer renderer, long millis, Operation<Void> original) {
        original.call(renderer, millis / 50L);
    }
}
