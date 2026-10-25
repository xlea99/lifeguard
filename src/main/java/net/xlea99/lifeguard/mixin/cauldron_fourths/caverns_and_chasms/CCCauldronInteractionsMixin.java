package net.xlea99.lifeguard.mixin.cauldron_fourths.caverns_and_chasms;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Caverns & Chasms' golden buckets pour a full layered cauldron (water, powder snow, Neapolitan's milk) at four levels
 * and only scoop a full one at four. These live in lambdas, so every method of the class is searched; its only 3s are
 * those levels (twelve of them).
 */
@Mixin(targets = "com.teamabnormals.caverns_and_chasms.core.other.CCCauldronInteractions")
public abstract class CCCauldronInteractionsMixin {

    @ModifyConstant(method = "*", constant = @Constant(intValue = 3), require = 12)
    private static int lifeguard$fullAtFour(int full) {
        return 4;
    }
}
