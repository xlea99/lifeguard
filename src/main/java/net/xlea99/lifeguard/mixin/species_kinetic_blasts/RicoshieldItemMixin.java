package net.xlea99.lifeguard.mixin.species_kinetic_blasts;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Ricoshield's release hits each target with all the damage it stored, scaled by that target's own distance (#19):
 * the same in-place scaling as the Quake's slam ({@code QuakeMixin}). The shield is one item for every player, on the
 * client and server threads alike, so the stored amount is kept per thread. Written against our build of Species'
 * 1.21.1 port (PR #88), by class name.
 */
@Mixin(targets = "com.ninni.species.server.item.RicoshieldItem")
public abstract class RicoshieldItemMixin {
    @Unique
    private static final ThreadLocal<Float> lifeguard$STORED = new ThreadLocal<>();

    @Inject(method = "damageTargets", at = @At("HEAD"))
    private void lifeguard$remember(ItemStack stack, Level level, float amount, LivingEntity player, CallbackInfo ci) {
        lifeguard$STORED.set(amount);
    }

    /** The one read of the amount before it's scaled for each target. */
    @ModifyVariable(method = "damageTargets", at = @At(value = "LOAD", ordinal = 0), argsOnly = true)
    private float lifeguard$unscaled(float amount) {
        Float stored = lifeguard$STORED.get();
        return stored != null ? stored : amount;
    }
}
