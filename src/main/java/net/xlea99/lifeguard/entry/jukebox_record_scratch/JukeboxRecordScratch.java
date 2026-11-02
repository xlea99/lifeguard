package net.xlea99.lifeguard.entry.jukebox_record_scratch;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;
import net.xlea99.lifeguard.entry.jukebox_synced_playback.JukeboxSyncedPlayback;

public final class JukeboxRecordScratch {
    /**
     * Strong: a joke the game tells once per scare. A creeper hissing right by a playing jukebox cuts the music with a
     * record scratch; once no creeper nearby is still hissing, a few seconds of awkward silence, then the song picks up
     * where it stopped. Needs synced playback to start the song again mid-way. The work is the server's. See
     * {@link RecordScratch}.
     */
    public static final Entry ENTRY = Entries.feature("jukebox_record_scratch", Taste.STRONG).home(Entry.MINECRAFT)
        .requiresEntries(JukeboxSyncedPlayback.ENTRY)
        .side(Side.SERVER).restart(Restart.GAME);

    private JukeboxRecordScratch() {
    }
}
