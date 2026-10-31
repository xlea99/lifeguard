package net.xlea99.lifeguard.entry.jukebox_progress_signal;

/** A jukebox remembering its last progress reading, to tell comparators only when it moves on. */
public interface ProgressJukebox {
    int lifeguard$lastProgress();

    void lifeguard$setLastProgress(int progress);
}
