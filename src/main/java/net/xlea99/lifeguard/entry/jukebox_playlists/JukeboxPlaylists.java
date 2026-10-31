package net.xlea99.lifeguard.entry.jukebox_playlists;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;
import net.xlea99.lifeguard.entry.jukebox_synced_playback.JukeboxSyncedPlayback;

public final class JukeboxPlaylists {
    /**
     * Light: Minecraft 26.4's own idea (a chest feeding a jukebox), taken further. Runtime-gated: the jukebox ticks on
     * both sides, each asking the switch the server synced (the work itself is the server's). See {@link Playlists}.
     */
    public static final Entry ENTRY = Entries.feature("jukebox_playlists", Taste.LIGHT)
        .requiresEntries(JukeboxSyncedPlayback.ENTRY)
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Right-click skips, sneak-right-click turns it off, right-clicking it empty turns it on. Clicks run on both sides. */
    public static final Entry RADIO_CONTROLS = Entries.tweak("radio_controls", Taste.STRONG).parent(ENTRY)
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** A dropper or dispenser (or Etcetera's dice) touching the jukebox shuffles its playlist: our own, not 26.4's. */
    public static final Entry SHUFFLE = Entries.feature("shuffle", Taste.STRONG).parent(ENTRY)
        .side(Side.SERVER).restart(Restart.GAME);

    private JukeboxPlaylists() {
    }
}
