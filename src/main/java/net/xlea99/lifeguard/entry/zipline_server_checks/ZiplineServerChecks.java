package net.xlea99.lifeguard.entry.zipline_server_checks;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ZiplineServerChecks {
    /**
     * None: Zipline relaxes the server's movement checks (flying kick, moved too quickly, moved wrongly) whenever a
     * player holds right-click with a pickaxe or hoe, chain or no chain; its ride runs on the client. This keeps the
     * relaxation only while the server itself finds a span under the player, at a zipline step. See {@link ChainedRides}.
     */
    public static final Entry ENTRY = Entries.fix("zipline_server_checks", Taste.NONE).home("zipline")
        .requiresMods("zipline")
        .side(Side.SERVER).restart(Restart.GAME);

    private ZiplineServerChecks() {
    }
}
