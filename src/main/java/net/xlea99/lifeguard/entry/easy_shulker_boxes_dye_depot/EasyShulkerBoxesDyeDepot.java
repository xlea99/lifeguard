package net.xlea99.lifeguard.entry.easy_shulker_boxes_dye_depot;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class EasyShulkerBoxesDyeDepot {
    /**
     * None: Dye Depot's 16 shulker boxes get Easy Shulker Boxes' handling, exactly as vanilla's colors have it. Ships
     * one Easy Shulker Boxes provider per color ({@code data/lifeguard/item_contents_provider/easy_shulker_boxes_dye_depot}),
     * each naming its box through an optional tag (so a pack without Dye Depot logs no parse errors). Their loader takes
     * no load conditions, so the entry's mixin drops them while it's off: runtime-gated, since that mixin must be there
     * exactly when the entry isn't on.
     */
    public static final Entry ENTRY = Entries.compat("easy_shulker_boxes_dye_depot", Taste.NONE).home("easyshulkerboxes")
        .requiresMods("easyshulkerboxes", "dye_depot")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private EasyShulkerBoxesDyeDepot() {
    }
}
