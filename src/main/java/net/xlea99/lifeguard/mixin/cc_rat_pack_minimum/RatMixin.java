package net.xlea99.lifeguard.mixin.cc_rat_pack_minimum;

import com.teamabnormals.caverns_and_chasms.common.entity.animal.rat.Rat;
import com.teamabnormals.caverns_and_chasms.core.CCConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * A rat pack spawned only if it gathered more than a hardcoded 6 rats (Rat.finalizeSpawn), so a pack that rolled the
 * minimum size, 6 by default, never came, and neither did any pack under a configured minimum below 7 (#44). Now any pack
 * of at least C&C's minimum pack size does. The second 6 in {@code finalizeSpawn}; the first is the pack's spawn range.
 */
@Mixin(Rat.class)
public abstract class RatMixin {
    @ModifyConstant(method = "finalizeSpawn", constant = @Constant(intValue = 6, ordinal = 1))
    private int lifeguard$packAtMinimumSize(int six) {
        return Math.max(0, CCConfig.COMMON.minimumRatPackSize.get() - 1);
    }
}
