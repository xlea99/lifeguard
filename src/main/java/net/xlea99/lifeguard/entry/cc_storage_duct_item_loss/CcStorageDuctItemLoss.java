package net.xlea99.lifeguard.entry.cc_storage_duct_item_loss;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcStorageDuctItemLoss {
    /** Hoppers and pipes move items on the server. See duct/StorageDucts. */
    public static final Entry ENTRY = Entries.fix("cc_storage_duct_item_loss", Taste.NONE)
        .requiresMods("caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME)
        .sharesMixins("cc_storage_ducts", "caverns_and_chasms");

    private CcStorageDuctItemLoss() {
    }
}
