package net.xlea99.lifeguard.entry.vanilla_backport_pause_pauses_music;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class VanillaBackportPausePausesMusic {
    /** Sound is the client's own. */
    public static final Entry ENTRY = Entries.tweak("vanilla_backport_pause_pauses_music", Taste.LIGHT)
        .requiresMods("vanillabackport")
        .side(Side.CLIENT).restart(Restart.GAME);

    private VanillaBackportPausePausesMusic() {
    }
}
