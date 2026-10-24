package net.xlea99.lifeguard.mixin.supps_lumisene_burn_time;

import net.mehvahdjukaar.supplementaries.common.fluids.FlammableLiquidBlock;
import net.xlea99.lifeguard.entry.supps_lumisene_burn_time.SuppsLumiseneBurnTime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Burning lumisene sets what's in it on fire for 8 seconds, as in Supplementaries 3.1 and like vanilla fire. 4.0.2's
 * port turned {@code setSecondsOnFire(8)} into {@code setRemainingFireTicks(8)}: 8 ticks, gone almost at once.
 */
@Mixin(FlammableLiquidBlock.class)
public abstract class FlammableLiquidBlockMixin {

    // The second call: the first only counts up the fire that's already there, as vanilla fire does.
    @ModifyArg(method = "entityInside",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setRemainingFireTicks(I)V", ordinal = 1))
    private int lifeguard$eightSeconds(int ticks) {
        return SuppsLumiseneBurnTime.ENTRY.active() ? 8 * 20 : ticks;
    }
}
