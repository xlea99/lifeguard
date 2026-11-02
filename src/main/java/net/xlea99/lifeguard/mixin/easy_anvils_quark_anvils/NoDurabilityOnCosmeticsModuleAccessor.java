package net.xlea99.lifeguard.mixin.easy_anvils_quark_anvils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.violetmoon.quark.content.tweaks.module.NoDurabilityOnCosmeticsModule;

/** Quark's own toggle for free cosmetics, which it keeps private. */
@Mixin(NoDurabilityOnCosmeticsModule.class)
public interface NoDurabilityOnCosmeticsModuleAccessor {
    @Accessor("allowCosmeticItems")
    boolean lifeguard$allowCosmeticItems();
}
