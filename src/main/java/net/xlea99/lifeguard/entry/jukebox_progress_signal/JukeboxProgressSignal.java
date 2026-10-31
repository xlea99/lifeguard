package net.xlea99.lifeguard.entry.jukebox_progress_signal;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;
import net.xlea99.lifeguard.entry.jukebox_synced_playback.JukeboxSyncedPlayback;

public final class JukeboxProgressSignal {
    /** Comparators are the server's. Needs synced playback, so the reading and the music never drift apart. */
    public static final Entry ENTRY = Entries.tweak("jukebox_progress_signal", Taste.STRONG).home(Entry.MINECRAFT)
        .requiresEntries(JukeboxSyncedPlayback.ENTRY)
        .side(Side.SERVER).restart(Restart.GAME);

    private JukeboxProgressSignal() {
    }
}
