package net.xlea99.lifeguard.mixin.etcetera_easy_anvils_label_costs;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.xlea99.lifeguard.entry.etcetera_easy_anvils_label_costs.LabelCosts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Prices Etcetera's label writing by Easy Anvils' rules ({@link LabelCosts}) when Easy Anvils hands the result to the
 * vanilla anvil code, where Etcetera writes the label in its own handler ({@code AnvilMenuMixin.hammeringAnvil}). Easy
 * Anvils' menu takes a result that costs nothing. Through MixinSquared; applied after Etcetera's mixin.
 */
@Mixin(value = AnvilMenu.class, priority = 1500)
public abstract class AnvilMenuMixin {

    @TargetHandler(mixin = "com.ninni.etcetera.mixin.AnvilMenuMixin", name = "hammeringAnvil")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V"))
    private void lifeguard$labelCost(DataSlot cost, int etceteraCost, Operation<Void> original) {
        original.call(cost, LabelCosts.cost(((ItemCombinerMenuAccessor) this).lifeguard$inputSlots().getItem(1), etceteraCost));
    }
}
