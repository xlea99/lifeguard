package net.xlea99.lifeguard.mixin.cauldron_fourths_alexs_caves;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** The Tremorsaurus' stomp ripples water in a cauldron at its surface: the four-level heights, (7 + 2 x level) / 16. */
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.entity.living.TremorsaurusEntity")
public abstract class TremorsaurusEntityMixin {

    @ModifyConstant(method = "getWaterLevelForBlock", constant = @Constant(floatValue = 6.0F))
    private float lifeguard$base(float base) {
        return 7.0F;
    }

    @ModifyConstant(method = "getWaterLevelForBlock", constant = @Constant(floatValue = 3.0F))
    private float lifeguard$perLevel(float perLevel) {
        return 2.0F;
    }
}
