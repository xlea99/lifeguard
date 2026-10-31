package net.xlea99.lifeguard.entry.cc_storage_duct_span_limit;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.SubValue;
import net.xlea99.lifeguard.core.Taste;

public final class CcStorageDuctSpanLimit {
    /** Chunks either way; up and down is free (chunks are whole columns). */
    public static final SubValue.Int MAX_SPAN = SubValue.ofInt("max_span", 8, 1, 64);
    /** Networks are built and opened on the server. See duct/StorageDucts. */
    public static final Entry ENTRY = Entries.tweak("cc_storage_duct_span_limit", Taste.LIGHT)
        .requiresMods("caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME)
        .sharesMixins("cc_storage_ducts", "caverns_and_chasms")
        .values(MAX_SPAN);

    private CcStorageDuctSpanLimit() {
    }
}
