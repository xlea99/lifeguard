package net.xlea99.lifeguard.mixin.etcetera_easy_anvils_label_costs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fuzs.easyanvils.handler.ItemInteractionHandler;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.etcetera_easy_anvils_label_costs.EtceteraEasyAnvilsLabelCosts;
import net.xlea99.lifeguard.entry.etcetera_easy_anvils_label_costs.LabelCosts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Writing an item label counts as renaming for Easy Anvils' {@code risk_free_anvil_renaming}: the anvil can't wear out
 * from it. Easy Anvils spares the anvil only when the right slot is empty (a plain rename); a written label there is the
 * same operation, words onto an item.
 */
@Mixin(ItemInteractionHandler.class)
public abstract class ItemInteractionHandlerMixin {

    @WrapOperation(method = "onAnvilUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private static boolean lifeguard$labelIsRenaming(ItemStack right, Operation<Boolean> original) {
        return original.call(right) || EtceteraEasyAnvilsLabelCosts.ENTRY.active() && LabelCosts.isWrittenLabel(right);
    }
}
