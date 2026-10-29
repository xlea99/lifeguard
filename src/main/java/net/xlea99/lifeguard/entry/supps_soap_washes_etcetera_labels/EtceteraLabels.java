package net.xlea99.lifeguard.entry.supps_soap_washes_etcetera_labels;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Etcetera's item labels: up to four tooltip lines an anvil writes into an item's custom data, as {@code Label1} to
 * {@code Label4} (Etcetera's AnvilMenuMixin; drawn by its tooltip handler).
 */
public final class EtceteraLabels {
    private static final String[] KEYS = {"Label1", "Label2", "Label3", "Label4"};

    private EtceteraLabels() {
    }

    public static boolean has(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null)
            return false;
        CompoundTag tag = data.copyTag();
        for (String key : KEYS)
            if (tag.contains(key))
                return true;
        return false;
    }

    /** Takes every label off, and the custom data with them when nothing else is in it (so the item stacks again). */
    public static void strip(ItemStack stack) {
        if (!has(stack))
            return;
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
        for (String key : KEYS)
            tag.remove(key);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }
}
