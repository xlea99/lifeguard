package net.xlea99.lifeguard.mixin.cc_tether_potion_compat;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.cc_tether_potion_compat.RiggedPotions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A tether, impact or trail potion used on any Moonlight tank (a Supplementaries jar or goblet, an Amendments cauldron,
 * ...) breaks open into it (see {@link RiggedPotions}). The block saves its tank when this returns true.
 */
@Mixin(SoftFluidTank.class)
public abstract class SoftFluidTankMixin {

    @Inject(method = "interactWithPlayer", at = @At("HEAD"), cancellable = true)
    private void lifeguard$breakOpen(Player player, InteractionHand hand, Level level, BlockPos pos,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (level != null && pos != null && RiggedPotions.breakIntoTank((SoftFluidTank) (Object) this, player, hand, level, pos))
            cir.setReturnValue(true);
    }
}
