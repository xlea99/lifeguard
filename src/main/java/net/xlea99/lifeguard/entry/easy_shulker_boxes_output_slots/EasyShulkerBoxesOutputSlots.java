package net.xlea99.lifeguard.entry.easy_shulker_boxes_output_slots;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class EasyShulkerBoxesOutputSlots {
    /**
     * None: right-clicking a stack with a container item (shulker box, bundle) on the cursor took the WHOLE stack,
     * put in what fit, and handed the rest back to the slot, which an output slot (furnace result, crafting result)
     * refuses: the rest was deleted (proven in Deep End 2026-11-01: 64 bismuth ingots into a box with room for 8, 56
     * gone). Now only what fits is taken, as vanilla's bundles do ({@link OutputSlotFit}); a slot that won't give part
     * of its stack (every output slot) gives nothing when it doesn't all fit. Runtime-gated: inventory clicks run on the
     * client too. (Item Interactions 21.1.4, bundled in Easy Shulker Boxes 21.1.3:
     * ItemInteractionHelper.overrideStackedOnOther.)
     */
    public static final Entry ENTRY = Entries.fix("easy_shulker_boxes_output_slots", Taste.NONE).home("easyshulkerboxes")
        .requiresMods("easyshulkerboxes")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private EasyShulkerBoxesOutputSlots() {
    }
}
