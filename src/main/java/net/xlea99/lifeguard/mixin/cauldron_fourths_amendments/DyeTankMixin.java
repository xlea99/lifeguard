package net.xlea99.lifeguard.mixin.cauldron_fourths_amendments;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * A dye dropped into a full dye cauldron first takes one unit out to make room, then mixes in: "full" is 4 units now,
 * not 3, or a full dye cauldron would refuse every new dye. (The dye tank is an anonymous class in the block entity.)
 */
@Mixin(targets = "net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile$2")
public abstract class DyeTankMixin {

    @ModifyConstant(method = "addDyeItem", constant = @Constant(intValue = 3))
    private int lifeguard$fullAtFour(int full) {
        return 4;
    }
}
