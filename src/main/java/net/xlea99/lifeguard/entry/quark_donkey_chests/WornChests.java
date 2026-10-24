package net.xlea99.lifeguard.entry.quark_donkey_chests;

import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.item.ItemStack;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.module.VariantChestsModule;

/**
 * Quark's chest variants on donkeys, mules and llamas. Putting a Quark chest on one stores it in the animal's data; the
 * animal still just "has a chest" as far as vanilla knows, and drops a plain one. Only reached from this entry's
 * mixins, which only load with Quark installed.
 */
public final class WornChests {
    /** Where Quark keeps the chest a chested horse wears, when it isn't a plain one. */
    private static final String TAG = "Quark:DonkChest";

    private WornChests() {
    }

    /** The Quark chest this animal wears, or empty: no chest, a plain one, or Quark's Variant Chests is off. */
    public static ItemStack worn(AbstractChestedHorse horse) {
        if (!horse.hasChest() || !Quark.ZETA.modules.isEnabled(VariantChestsModule.class))
            return ItemStack.EMPTY;
        return ItemStack.parseOptional(horse.level().registryAccess(), horse.getPersistentData().getCompound(TAG));
    }

    /** Forgets which Quark chest this animal wore, once that chest is gone. */
    public static void forget(AbstractChestedHorse horse) {
        horse.getPersistentData().remove(TAG);
    }
}
