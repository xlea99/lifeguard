package net.xlea99.lifeguard.entry.quark_donkey_chests;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class QuarkDonkeyChests {
    /** Death drops are the server's; the client half (forgetting a removed chest) is harmless either way. */
    public static final Entry ENTRY = Entries.fix("quark_donkey_chests", Taste.NONE).home("quark")
        .requiresMods("quark")
        .side(Side.SERVER).restart(Restart.GAME);

    private QuarkDonkeyChests() {
    }
}
