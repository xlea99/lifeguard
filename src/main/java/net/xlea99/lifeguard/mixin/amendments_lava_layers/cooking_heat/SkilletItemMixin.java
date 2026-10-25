package net.xlea99.lifeguard.mixin.amendments_lava_layers.cooking_heat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import net.xlea99.lifeguard.entry.amendments_lava_layers.CookingHeat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.item.SkilletItem;

/** A held skillet cooks next to a cauldron of lava (see {@link CookingHeat}). */
@Mixin(SkilletItem.class)
public abstract class SkilletItemMixin {

    @Inject(method = "isPlayerNearHeatSource", at = @At("RETURN"), cancellable = true)
    private static void lifeguard$nearLava(Player player, LevelReader level, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && CookingHeat.nearPlayer(player, level))
            cir.setReturnValue(true);
    }
}
