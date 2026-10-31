package net.xlea99.lifeguard.mixin.species_swapper_resets_fall;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A Wicked Swapper's swap ends a fall, as an ender pearl's teleport or chorus fruit's does (#29): a player who swapped
 * places with a mob on the ground while falling still took the whole fall's damage. Both ends of the swap (the mob and
 * the thrower) land with no fall behind them. Written against our build of Species' 1.21.1 port (PR #88), by class name.
 */
@Mixin(targets = "com.ninni.species.server.entity.mob.update_3.WickedSwapperProjectile")
public abstract class WickedSwapperProjectileMixin {
    @WrapOperation(method = "onHit(Lnet/minecraft/world/phys/HitResult;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;teleportTo(DDD)V"))
    private void lifeguard$swapEndsFall(Entity entity, double x, double y, double z, Operation<Void> original) {
        original.call(entity, x, y, z);
        entity.resetFallDistance();
    }
}
