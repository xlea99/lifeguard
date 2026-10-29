package net.xlea99.lifeguard.mixin.etcetera_easy_anvils_label_costs;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.xlea99.lifeguard.entry.etcetera_easy_anvils_label_costs.LabelCosts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Prices Etcetera's label writing by Easy Anvils' rules ({@link LabelCosts}) in Easy Anvils' own result code, where
 * Etcetera writes the label in its own handler ({@code easyanvils.EasyAnvilsCompatMixin.easyAnvils$hammeringAnvil}).
 * Through MixinSquared; applied after Etcetera's mixin.
 */
@Mixin(value = ModAnvilMenu.class, priority = 1500)
public abstract class ModAnvilMenuMixin {

    @TargetHandler(mixin = "com.ninni.etcetera.mixin.easyanvils.EasyAnvilsCompatMixin", name = "easyAnvils$hammeringAnvil")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V"))
    private void lifeguard$labelCost(DataSlot cost, int etceteraCost, Operation<Void> original) {
        original.call(cost, LabelCosts.cost(((ItemCombinerMenuAccessor) this).lifeguard$inputSlots().getItem(1), etceteraCost));
    }
}
