package net.xlea99.lifeguard.entry.no_supps_jar_fish;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class NoSuppsJarFish {
    /** Runtime-gated: jars predict the bucket on the client, so the client follows the server's setting. */
    public static final Entry ENTRY = Entries.tweak("no_supps_jar_fish", Taste.DEEP_END)
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private NoSuppsJarFish() {
    }
}
