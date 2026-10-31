package net.xlea99.lifeguard.mixin.species_kinetic_blasts;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A Quake's slam hits each target with all the damage it stored, scaled by that target's own distance (#19).
 *
 * <p>The slam scales its damage by distance in place ({@code amount *= scalingFactor}, inside the loop over targets), so
 * every target also took the scaling of every target hit before it: two mobs at the edge ahead of the player cut the
 * player's share to a sixteenth. The amount is read back as stored at the start of the slam each time it's scaled, so
 * each target gets only its own. Species' Deflector Dummy already scales each target on its own; the Ricoshield is
 * {@code RicoshieldItemMixin}. Written against our build of Species' 1.21.1 port (PR #88), by class name.
 */
@Mixin(targets = "com.ninni.species.server.entity.mob.update_3.Quake")
public abstract class QuakeMixin {
    @Unique
    private float lifeguard$stored;

    @Inject(method = "damage(F)V", at = @At("HEAD"))
    private void lifeguard$remember(float amount, CallbackInfo ci) {
        this.lifeguard$stored = amount;
    }

    /** The one read of the amount before it's scaled for each target. */
    @ModifyVariable(method = "damage(F)V", at = @At(value = "LOAD", ordinal = 0), argsOnly = true)
    private float lifeguard$unscaled(float amount) {
        return this.lifeguard$stored;
    }
}
