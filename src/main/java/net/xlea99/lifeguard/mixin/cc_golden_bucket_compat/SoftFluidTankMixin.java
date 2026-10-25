package net.xlea99.lifeguard.mixin.cc_golden_bucket_compat;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.cc_golden_bucket_compat.GoldenBuckets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A golden bucket used on any Moonlight tank (a Supplementaries jar, an Amendments cauldron, ...) pours or scoops a
 * whole bucket (see {@link GoldenBuckets}). The block saves its tank when this returns true.
 */
@Mixin(SoftFluidTank.class)
public abstract class SoftFluidTankMixin {

    @Inject(method = "interactWithPlayer", at = @At("HEAD"), cancellable = true)
    private void lifeguard$goldenBuckets(Player player, InteractionHand hand, Level level, BlockPos pos,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (level != null && pos != null && GoldenBuckets.useOnTank((SoftFluidTank) (Object) this, player, hand, level, pos))
            cir.setReturnValue(true);
    }
}
