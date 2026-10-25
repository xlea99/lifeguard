package net.xlea99.lifeguard.entry.moonlight_jade_contents;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class MoonlightJadeContents {
    /** Only what Jade shows; the contents are already on the client. */
    public static final Entry ENTRY = Entries.compat("moonlight_jade_contents", Taste.NONE)
        .requiresMods("moonlight", "jade")
        .side(Side.CLIENT).restart(Restart.GAME);

    private MoonlightJadeContents() {
    }
}
