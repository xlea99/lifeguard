package net.xlea99.lifeguard.entry.jukebox_progress_signal;

import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

/**
 * A comparator reads how far through its song a jukebox is, instead of the song's own number (which, with dozens of
 * modded discs sharing 15 values, tells you nothing). 1 as a song starts, climbing evenly to 15 when it's over, and 15
 * stays until the disc comes out: a line of dust off the comparator is a progress bar, and 15 is "the song finished".
 * An empty jukebox is 0. A song that never really ends (Quark's ambience discs) stays at 1.
 */
public final class JukeboxProgress {
    private JukeboxProgress() {
    }

    /** The reading for {@code jukebox} right now. */
    public static int of(JukeboxBlockEntity jukebox) {
        if (jukebox.getTheItem().isEmpty())
            return 0;
        JukeboxSongPlayer player = jukebox.getSongPlayer();
        JukeboxSong song = player.getSong();
        return song == null ? 15 : signal(player.getTicksSinceSongStarted(), song.lengthInTicks());
    }

    /** The signal {@code elapsed} ticks into a song {@code length} ticks long: 15 only once it's over. */
    static int signal(long elapsed, int length) {
        if (length <= 0 || elapsed >= length)
            return 15;
        return 1 + (int) (Math.max(elapsed, 0) * 14 / length);
    }
}
