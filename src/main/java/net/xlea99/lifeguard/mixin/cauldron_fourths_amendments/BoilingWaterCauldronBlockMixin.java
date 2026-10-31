package net.xlea99.lifeguard.mixin.cauldron_fourths_amendments;

import net.mehvahdjukaar.amendments.common.block.BoilingWaterCauldronBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Crafting by hand in a water cauldron counts a full one as 4 units, not 3. */
@Mixin(BoilingWaterCauldronBlock.class)
public abstract class BoilingWaterCauldronBlockMixin {

    @ModifyConstant(method = "useItemOn", constant = @Constant(intValue = 3))
    private int lifeguard$fourUnits(int full) {
        return 4;
    }
}
