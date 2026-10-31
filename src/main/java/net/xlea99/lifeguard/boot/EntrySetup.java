package net.xlea99.lifeguard.boot;

/**
 * An entry's setup code: the listeners, registrations and hooks it needs when the mod is constructed. It lives beside
 * the declaration it serves, named after it: {@code JukeboxSyncedPlaybackSetup} for the common side,
 * {@code JukeboxSyncedPlaybackClientSetup} for the client only (never loaded on a server). {@link Setups} finds both by
 * name from the {@link Catalog}, so no central file lists them.
 *
 * <p>Kept apart from the declaration on purpose: declarations load before Minecraft's classes do (the mixin plugin
 * reads them), and setup code is full of Minecraft.
 */
public interface EntrySetup {
    void setup(Wiring wiring);
}
