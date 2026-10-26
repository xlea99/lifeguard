package net.xlea99.lifeguard.entry.haunted_harvest_mask_limit;

import net.mehvahdjukaar.hauntedharvest.items.components.PumpkinCarvingData;
import net.mehvahdjukaar.hauntedharvest.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Over-carved Haunted Harvest pumpkins aren't ender masks ({@link HauntedHarvestMaskLimit};
 * {@code mixin.haunted_harvest_mask_limit.ModCarvedPumpkinItemMixin}).
 *
 * <p>A worn custom pumpkin's view is its carving: every carved pixel is a clear spot, so carving everything out left
 * almost nothing in the way, while the pumpkin still counted as an ender mask. Vanilla's pumpkin overlay leaves 30.9% of
 * the screen clear, which is 100 of the carving's 256 pixels; a carving with more than the maximum no longer fools
 * endermen, and its tooltip says so.
 */
public final class HollowPumpkins {
    private HollowPumpkins() {
    }

    public static void registerTooltip() {
        NeoForge.EVENT_BUS.addListener((ItemTooltipEvent event) -> {
            if (tooOpen(event.getItemStack()))
                event.getToolTip().add(Math.min(1, event.getToolTip().size()),
                    Component.translatable("lifeguard.tooltip.carved_pumpkin_too_open").withStyle(ChatFormatting.GRAY));
        });
    }

    /** True if this is a Haunted Harvest pumpkin carved past the maximum (and the entry's on). */
    public static boolean tooOpen(ItemStack stack) {
        if (!HauntedHarvestMaskLimit.ENTRY.active())
            return false;
        PumpkinCarvingData carving = stack.get(ModRegistry.PUMPKIN_CARVING.get());
        return carving != null && carved(carving) > HauntedHarvestMaskLimit.MAX_CARVED.get();
    }

    private static int carved(PumpkinCarvingData carving) {
        int carved = 0;
        for (boolean[] column : carving.clonePixels())
            for (boolean pixel : column)
                if (pixel)
                    carved++;
        return carved;
    }
}
