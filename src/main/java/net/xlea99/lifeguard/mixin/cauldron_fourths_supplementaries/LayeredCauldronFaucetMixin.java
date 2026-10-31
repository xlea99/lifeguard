package net.xlea99.lifeguard.mixin.cauldron_fourths_supplementaries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Faucets fill water and powder snow cauldrons up to four levels, not three (they'd stall at three of four otherwise).
 * Only {@code fill}: the 3 in {@code drain} is block update flags, not a level.
 */
@Mixin(targets = {
    "net.mehvahdjukaar.supplementaries.common.block.faucet.WaterCauldronInteraction",
    "net.mehvahdjukaar.supplementaries.common.block.faucet.PowderSnowCauldronInteraction"
})
public abstract class LayeredCauldronFaucetMixin {

    @ModifyConstant(method = "fill", constant = @Constant(intValue = 3))
    private int lifeguard$fullAtFour(int full) {
        return 4;
    }
}
