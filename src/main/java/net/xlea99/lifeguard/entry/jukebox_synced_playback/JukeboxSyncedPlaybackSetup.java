package net.xlea99.lifeguard.entry.jukebox_synced_playback;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link JukeboxSyncedPlayback}. */
public final class JukeboxSyncedPlaybackSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // Runtime-gated: the payload is registered whenever it could be sent; the listeners ask the switch.
        wiring.whenInstalled(JukeboxSyncedPlayback.ENTRY, bus -> bus.addListener(JukeboxSyncPayload::register));
    }
}
