package net.xlea99.lifeguard.mixin.quark_tool_restock_enchantments;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.content.management.module.AutomaticToolRestockModule;

/**
 * Quark's Automatic Tool Restock replaces a broken tool with one sharing its important enchantments (Silk Touch, Fortune,
 * ...) again, as in Quark 4.0. Quark 4.1 asks an item for each enchantment through {@code Holder.direct(enchantment)}, a
 * fresh holder that never equals the registry's holder the item actually stores, so every tool read as having none and
 * the first tool of the same kind won. This hands the lookup the registry's own holder.
 */
@Mixin(AutomaticToolRestockModule.class)
public abstract class AutomaticToolRestockModuleMixin {

    @WrapOperation(method = "getImportantEnchantments",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;direct(Ljava/lang/Object;)Lnet/minecraft/core/Holder;"))
    private Holder<?> lifeguard$registryHolder(Object enchantment, Operation<Holder<?>> original,
                                               @Local(argsOnly = true) RegistryAccess access) {
        return access.registryOrThrow(Registries.ENCHANTMENT).wrapAsHolder((net.minecraft.world.item.enchantment.Enchantment) enchantment);
    }
}
