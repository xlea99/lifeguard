package net.xlea99.lifeguard.mixin.species_wicked_spares_pets;

import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A Wicked leaves pets alone (#28).
 *
 * <p>A Wicked haunts a hostile or neutral mob, which then fights for it: it's set on the Wicked's target, a player.
 * Species keeps wolves, cats and ocelots out of it by name ({@code #species:cant_be_haunted}), but every other mod's pet
 * was fair game, and a haunted pet turned on its owner. A mob with an owner can't be haunted now, and one already haunted
 * is let go (with its Wicked effects taken off), as it would be if it had been named. Written against our build of
 * Species' 1.21.1 port (PR #88), by class name.
 */
@Mixin(targets = "com.ninni.species.server.entity.mob.update_3.Wicked")
public abstract class WickedMixin {
    @Shadow
    public abstract UUID getHauntedTargetUUID();

    @Shadow
    public abstract void setHauntedTargetUUID(UUID uuid);

    @Shadow
    public abstract void removeWickedEffect(Mob target);

    @Inject(method = "isSuitableForHaunting", at = @At("RETURN"), cancellable = true)
    private void lifeguard$sparePets(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !(entity instanceof OwnableEntity pet) || pet.getOwnerUUID() == null)
            return;
        cir.setReturnValue(false);
        // A Wicked saved while haunting a pet looks for it again by this check after a reload, and wouldn't find it: the
        // pet would keep its Wicked effect for good. Let it go here instead.
        if (!((Entity) (Object) this).level().isClientSide() && entity instanceof Mob mob && mob.getUUID().equals(getHauntedTargetUUID())) {
            removeWickedEffect(mob);
            setHauntedTargetUUID(null);
        }
    }
}
