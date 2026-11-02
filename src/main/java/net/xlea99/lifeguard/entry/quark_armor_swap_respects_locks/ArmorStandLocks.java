package net.xlea99.lifeguard.entry.quark_armor_swap_respects_locks;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

/** An armor stand's DisabledSlots lock, read the way vanilla's own right-click reads it. */
public final class ArmorStandLocks {
    private ArmorStandLocks() {
    }

    /**
     * Whether {@code stand} forbids swapping {@code slot}, by vanilla's rules (ArmorStand.isDisabled and swapItem): the
     * slot is disabled, it's a hand on a stand without arms, its item can't be taken, or it's empty and can't take the
     * player's {@code worn} item.
     */
    public static boolean locked(ArmorStand stand, int disabledSlots, EquipmentSlot slot, ItemStack worn) {
        int flag = slot.getFilterFlag();
        if ((disabledSlots & 1 << flag) != 0 || slot.getType() == EquipmentSlot.Type.HAND && !stand.isShowArms())
            return true;
        if (!stand.getItemBySlot(slot).isEmpty())
            return (disabledSlots & 1 << flag + 8) != 0;
        return !worn.isEmpty() && (disabledSlots & 1 << flag + 16) != 0;
    }
}
