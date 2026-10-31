package net.xlea99.lifeguard.entry.supps_faucet_toggle;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsFaucetToggle {
    /** The faucet's state is the server's. */
    public static final Entry ENTRY = Entries.fix("supps_faucet_toggle", Taste.NONE).home("supplementaries")
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME);

    private SuppsFaucetToggle() {
    }
}
