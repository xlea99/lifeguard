package net.xlea99.lifeguard.entry.easy_anvils_quark_anvils;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class EasyAnvilsQuarkAnvils {
    /**
     * None: no new rule, just one order for two mods that set the same number in whichever order they loaded. Easy
     * Anvils decides renaming, Quark's No Durability On Cosmetics decides runes. See {@link AnvilBreaks}.
     */
    public static final Entry ENTRY = Entries.compat("easy_anvils_quark_anvils", Taste.NONE).home("easyanvils")
        .requiresMods("easyanvils", "quark")
        .side(Side.SERVER).restart(Restart.GAME);

    private EasyAnvilsQuarkAnvils() {
    }
}
