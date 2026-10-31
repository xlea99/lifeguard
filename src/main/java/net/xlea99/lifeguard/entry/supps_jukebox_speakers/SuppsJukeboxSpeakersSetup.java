package net.xlea99.lifeguard.entry.supps_jukebox_speakers;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;
import net.xlea99.lifeguard.entry.jukebox_synced_playback.JukeboxSync;

/** Setup for {@link SuppsJukeboxSpeakers}. */
public final class SuppsJukeboxSpeakersSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenInstalled(SuppsJukeboxSpeakers.ENTRY, () -> JukeboxSync.addCarrier(JukeboxSpeakers::carry));
    }
}
