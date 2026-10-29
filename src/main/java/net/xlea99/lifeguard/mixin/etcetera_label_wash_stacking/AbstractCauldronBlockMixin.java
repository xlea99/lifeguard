package net.xlea99.lifeguard.mixin.etcetera_label_wash_stacking;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.xlea99.lifeguard.entry.etcetera_label_wash_stacking.EtceteraLabelWashStacking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Items washed of their Etcetera item labels at a water cauldron stack with unlabeled ones again.
 *
 * <p>Etcetera's wash ({@code AbstractCauldronBlockMixin.etcetera$useItemOn}) takes the label lines out of the item's
 * custom data and sets what's left back, even when nothing is left: an empty custom data component is still a component,
 * so the washed item no longer matches a never-labeled one and won't stack with it. Vanilla removes the component when
 * it empties (CustomData.set); this does the same inside Etcetera's handler, through MixinSquared. Applied after
 * Etcetera's mixin (higher priority), whose handler this is.
 */
@Mixin(value = AbstractCauldronBlock.class, priority = 1500)
public abstract class AbstractCauldronBlockMixin {

    @TargetHandler(mixin = "com.ninni.etcetera.mixin.AbstractCauldronBlockMixin", name = "etcetera$useItemOn")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object lifeguard$dropEmptyCustomData(ItemStack stack, DataComponentType<?> type, Object value, Operation<Object> original) {
        if (EtceteraLabelWashStacking.ENTRY.active() && type == DataComponents.CUSTOM_DATA && value instanceof CustomData data
            && data.isEmpty())
            return stack.remove(DataComponents.CUSTOM_DATA);
        return original.call(stack, type, value);
    }
}
