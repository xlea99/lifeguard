package net.xlea99.lifeguard.mixin.cauldron_fourths_amendments;

import net.mehvahdjukaar.amendments.common.block.CommonCauldronCode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Crafting by dropping items into a cauldron counts a full cauldron as 4 units, not 3. The same method carried a
 * Forge-only step turning a 3-unit water result back into 4, a patch over the old mismatch: at 4 it compares 4 with 4
 * and sets 4, nothing.
 */
@Mixin(CommonCauldronCode.class)
public abstract class CauldronCraftingMixin {

    @ModifyConstant(method = "attemptInWorldCrafting", constant = @Constant(intValue = 3))
    private static int lifeguard$fourUnits(int full) {
        return 4;
    }
}
