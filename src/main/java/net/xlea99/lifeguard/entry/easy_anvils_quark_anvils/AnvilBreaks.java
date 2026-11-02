package net.xlea99.lifeguard.entry.easy_anvils_quark_anvils;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.ServerConfig;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import net.xlea99.lifeguard.mixin.easy_anvils_quark_anvils.NoDurabilityOnCosmeticsModuleAccessor;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tweaks.module.NoDurabilityOnCosmeticsModule;

/**
 * Whether a used anvil can break, settled the same way every time. Easy Anvils always sets its own chance, and Quark's
 * No Durability On Cosmetics makes renames and runes free, both in the same event at the same priority (Puzzles Lib and
 * Zeta both use normal), so which ran last came down to the order the two mods happened to load in (proven from the
 * sources, Easy Anvils 21.1.0, Quark 4.1, Zeta 1.1). This runs after both: renaming follows Easy Anvils'
 * risk_free_anvil_renaming (the anvil mod's own setting for exactly that), and a cosmetic Quark makes free (a rune, with
 * Quark's module and its allowCosmeticItems on) stays free, since Easy Anvils has no say on cosmetics. Anything else
 * keeps the chance Easy Anvils set.
 */
public final class AnvilBreaks {
    private AnvilBreaks() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, AnvilBreaks::settle);
    }

    private static void settle(AnvilRepairEvent event) {
        if (event.getEntity().level().isClientSide)
            return;
        ServerConfig.MiscellaneousConfig easyAnvils = EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous;
        if (event.getRight().isEmpty())
            event.setBreakChance(easyAnvils.riskFreeAnvilRenaming ? 0.0F : (float) easyAnvils.anvilBreakChance);
        else if (freeCosmetic(event.getRight()))
            event.setBreakChance(0.0F);
    }

    /** Whether Quark's No Durability On Cosmetics, by its own toggles, makes this right-slot item free. */
    private static boolean freeCosmetic(ItemStack right) {
        var modules = Quark.ZETA.modules;
        return modules.isEnabled(NoDurabilityOnCosmeticsModule.class)
            && ((NoDurabilityOnCosmeticsModuleAccessor) modules.get(NoDurabilityOnCosmeticsModule.class))
                .lifeguard$allowCosmeticItems()
            && right.is(NoDurabilityOnCosmeticsModule.cosmeticTag);
    }
}
