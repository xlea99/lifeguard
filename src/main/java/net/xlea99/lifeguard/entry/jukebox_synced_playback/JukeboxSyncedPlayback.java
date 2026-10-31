package net.xlea99.lifeguard.entry.jukebox_synced_playback;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class JukeboxSyncedPlayback {
    /**
     * Runtime-gated: the server tells listeners where its jukeboxes are in their songs, and each client plays along,
     * both asking the switch the server synced. See {@link JukeboxSync}.
     */
    public static final Entry ENTRY = Entries.feature("jukebox_synced_playback", Taste.LIGHT).home(Entry.MINECRAFT)
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private JukeboxSyncedPlayback() {
    }
}
