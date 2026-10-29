package net.xlea99.lifeguard.mixin.supps_soap_washes_etcetera_labels.no_cauldron_wash;

import com.bawnorton.mixinsquared.TargetHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.xlea99.lifeguard.entry.supps_soap_washes_etcetera_labels.SuppsSoapWashesEtceteraLabels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A water cauldron no longer washes Etcetera's item labels off; soap does. Etcetera's wash
 * ({@code AbstractCauldronBlockMixin.etcetera$useItemOn}) returns before doing anything, so the click goes on to the
 * cauldron's own use of the item (a dye dyes the water, a bucket fills, anything else does nothing), the same as for an
 * unlabeled item. Through MixinSquared; applied after Etcetera's mixin (higher priority), whose handler this is.
 */
@Mixin(value = AbstractCauldronBlock.class, priority = 1500)
public abstract class AbstractCauldronBlockMixin {

    @TargetHandler(mixin = "com.ninni.etcetera.mixin.AbstractCauldronBlockMixin", name = "etcetera$useItemOn")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void lifeguard$noLabelWash(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                       InteractionHand hand, BlockHitResult hit,
                                       CallbackInfoReturnable<ItemInteractionResult> washResult, CallbackInfo ci) {
        if (SuppsSoapWashesEtceteraLabels.NO_CAULDRON_WASH.active())
            ci.cancel();
    }
}
