package net.xlea99.lifeguard.mixin.cauldron_fourths.amendments;

import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Amendments' dye cauldron shares the cauldron level the vanilla cauldrons use, so it has four levels too: its tank
 * holds 4 units, not 3, or a full dye cauldron would claim more than its tank.
 */
@Mixin(LiquidCauldronBlockTile.class)
public abstract class LiquidCauldronBlockTileMixin {

    @ModifyConstant(method = "createCauldronDyeTank", constant = @Constant(intValue = 3))
    private int lifeguard$fourUnitDyeTank(int capacity) {
        return 4;
    }
}
