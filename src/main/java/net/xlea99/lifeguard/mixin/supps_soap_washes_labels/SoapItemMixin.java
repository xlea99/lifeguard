package net.xlea99.lifeguard.mixin.supps_soap_washes_labels;

import net.mehvahdjukaar.labels.LabelEntity;
import net.mehvahdjukaar.supplementaries.common.items.SoapItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Soap used on a dyed label washes its dye off, with the sound, bubbles and soap use of soap washing a mob. Sneaking with
 * soap still puts soap on the label as its picture, as with any item. Supplementaries runs this for every entity click
 * with soap, on both sides, before the label hears the click.
 */
@Mixin(SoapItem.class)
public abstract class SoapItemMixin {

    @Shadow
    private static void playEffectsAndConsume(ItemStack stack, Player player, Entity entity) {
    }

    @Inject(method = "interactWithEntity", at = @At("HEAD"), cancellable = true)
    private static void lifeguard$washLabel(ItemStack stack, Player player, Entity target, InteractionHand hand,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (!(target instanceof LabelEntity label) || label.getColor() == null || player.isSecondaryUseActive())
            return;
        if (!player.level().isClientSide)
            ((LabelEntityAccessor) label).lifeguard$setColor(null);
        playEffectsAndConsume(stack, player, target);
        cir.setReturnValue(true);
    }
}
