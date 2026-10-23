package net.xlea99.lifeguard.core;

import java.util.Locale;

/** What a change to an entry needs before it applies. Nothing applies live. */
public enum Restart {
    /** Anything registered or mixed in: quit the game. */
    GAME,
    /** Data-driven entries: leave and rejoin the world. */
    WORLD;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
