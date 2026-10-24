package net.xlea99.lifeguard.mixin.supps_drinkables_sip_pacing;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.supps_drinkables_sip_pacing.SipPacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A sip from a placed jar, at the drink's own pace (see {@link SipPacing}). */
@Mixin(JarBlockTile.class)
public abstract class JarBlockTileMixin {

    @WrapOperation(method = "handleInteraction",
        at = @At(value = "INVOKE",
            target = "Lnet/mehvahdjukaar/moonlight/api/fluids/SoftFluidTank;tryDrinkUpFluid(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;)Z"))
    private boolean lifeguard$sip(SoftFluidTank tank, Player player, Level level, Operation<Boolean> original) {
        return SipPacing.sip(tank, player, level, original);
    }
}
