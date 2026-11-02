package net.xlea99.lifeguard.mixin.zetter_paintings_stay_put;

import me.dantaeusb.zetter.entity.item.PaintingEntity;
import net.minecraft.world.entity.Entity;
import net.xlea99.lifeguard.entry.zetter_paintings_stay_put.ZetterPaintingsStayPut;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A Zetter painting's position update goes to Zetter's own lerpTo (the 1.20.1-shaped one 1.21.1 never calls) instead
 * of Entity's default. See {@link ZetterPaintingsStayPut}.
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "lerpTo(DDDFFI)V", at = @At("HEAD"), cancellable = true)
    private void lifeguard$zetterPaintingsUseTheirOwnLerp(double x, double y, double z, float yaw, float pitch,
                                                         int steps, CallbackInfo ci) {
        if ((Object) this instanceof PaintingEntity painting) {
            painting.lerpTo(x, y, z, yaw, pitch, steps, false);
            ci.cancel();
        }
    }
}
