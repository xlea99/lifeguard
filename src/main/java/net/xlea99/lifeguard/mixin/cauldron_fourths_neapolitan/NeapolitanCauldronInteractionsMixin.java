package net.xlea99.lifeguard.mixin.cauldron_fourths_neapolitan;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Neapolitan's milk and milkshake cauldrons are layered cauldrons too, so they have four levels: a milk bucket fills
 * one to 4, an empty bucket takes from 4, and a bottle won't pour into 4 (past it, the level would wrap back to 1 and
 * eat the bottle). These live in lambdas, so every method of the class is searched; its only 3s are those levels.
 */
@Mixin(targets = "com.teamabnormals.neapolitan.core.other.NeapolitanCauldronInteractions")
public abstract class NeapolitanCauldronInteractionsMixin {

    @ModifyConstant(method = "*", constant = @Constant(intValue = 3), require = 3)
    private static int lifeguard$fullAtFour(int full) {
        return 4;
    }
}
