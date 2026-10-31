package net.xlea99.lifeguard.mixin.cauldron_fourths_neapolitan;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** A milk cauldron's surface (where it works on what's in it) sits at the four-level heights: (7 + 2 x level) / 16. */
@Mixin(targets = "com.teamabnormals.neapolitan.common.block.MilkCauldronBlock")
public abstract class MilkCauldronBlockMixin {

    @ModifyConstant(method = "entityInside", constant = @Constant(floatValue = 6.0F))
    private float lifeguard$base(float base) {
        return 7.0F;
    }

    @ModifyConstant(method = "entityInside", constant = @Constant(intValue = 3))
    private int lifeguard$perLevel(int perLevel) {
        return 2;
    }
}
