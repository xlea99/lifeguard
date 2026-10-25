package net.xlea99.lifeguard.mixin.cauldron_fourths.supplementaries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** A cauldron moved into water (by rope or piston, with Supplementaries' moved_cauldron_filling) fills to four levels. */
@Mixin(targets = "net.mehvahdjukaar.supplementaries.common.block.cauldron.MovedFluidFiller")
public abstract class MovedFluidFillerMixin {

    @ModifyConstant(method = "fillIfMovedIntoFluid", constant = @Constant(intValue = 3))
    private static int lifeguard$fullAtFour(int full) {
        return 4;
    }
}
