package net.xlea99.lifeguard.entry.quark_tool_restock_enchantments;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class QuarkToolRestockEnchantments {
    /** Restocking happens on the server only. */
    public static final Entry ENTRY = Entries.fix("quark_tool_restock_enchantments", Taste.NONE).home("quark")
        .requiresMods("quark")
        .side(Side.SERVER).restart(Restart.GAME);

    private QuarkToolRestockEnchantments() {
    }
}
