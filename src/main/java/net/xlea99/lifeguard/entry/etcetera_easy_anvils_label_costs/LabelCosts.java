package net.xlea99.lifeguard.entry.etcetera_easy_anvils_label_costs;

import com.ninni.etcetera.registry.EtceteraItems;
import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.config.FreeRenames;
import fuzs.easyanvils.config.ServerConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

/**
 * Writing an Etcetera item label onto an item, priced by Easy Anvils' renaming rules ({@link EtceteraEasyAnvilsLabelCosts}).
 *
 * <p>A label is a name tag for items, so it follows Easy Anvils the way a name tag does: free whenever {@code free_renames}
 * makes name tags free (ALL_ITEMS and NAME_TAGS_ONLY), and risk-free for the anvil whenever renaming is. With
 * {@code free_renames} = NEVER, Etcetera's own price (1, 2, 3, then 4 levels per line) stands.
 */
public final class LabelCosts {
    private LabelCosts() {
    }

    /** A label with words on it, in the anvil's right slot: what Etcetera writes onto the left item. */
    public static boolean isWrittenLabel(ItemStack stack) {
        return stack.is(EtceteraItems.ITEM_LABEL.get()) && stack.has(DataComponents.CUSTOM_NAME);
    }

    /** Etcetera's price for writing {@code right} onto an item, or nothing if Easy Anvils makes it free. */
    public static int cost(ItemStack right, int etceteraCost) {
        if (!EtceteraEasyAnvilsLabelCosts.ENTRY.active() || !right.is(EtceteraItems.ITEM_LABEL.get()))
            return etceteraCost;
        return EasyAnvils.CONFIG.get(ServerConfig.class).costs.freeRenames == FreeRenames.NEVER ? etceteraCost : 0;
    }
}
