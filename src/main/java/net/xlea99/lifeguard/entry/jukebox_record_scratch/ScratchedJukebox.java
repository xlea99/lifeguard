package net.xlea99.lifeguard.entry.jukebox_record_scratch;

/** A jukebox's song player that a creeper can scratch ({@link RecordScratch}). */
public interface ScratchedJukebox {
    /** Scratches its song to a stop, keeping its place: false if it's already scratched (nothing new to hear). */
    boolean lifeguard$scratch();
}
