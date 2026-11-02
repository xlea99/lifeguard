package net.xlea99.lifeguard.entry.quark_armor_swap_respects_locks;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class QuarkArmorSwapRespectsLocks {
    /**
     * None: Quark's Quick Armor Swapping (sneak + use on an armor stand) swapped every slot, ignoring the stand's
     * DisabledSlots lock, which vanilla's own right-click obeys (set by commands, or Armor Statues' "Sealed"): a locked
     * display stand could be stripped. Now a slot the stand has locked is skipped ({@link ArmorStandLocks}; proven
     * 2026-10-31 by lockprobe: an unlocked control swapped, a 4144959 stand swapped too). Runtime-gated: the swap runs on
     * the client too. (Quark 4.1-488, QuickArmorSwappingModule.swapSlot.)
     */
    public static final Entry ENTRY = Entries.fix("quark_armor_swap_respects_locks", Taste.NONE).home("quark")
        .requiresMods("quark")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private QuarkArmorSwapRespectsLocks() {
    }
}
