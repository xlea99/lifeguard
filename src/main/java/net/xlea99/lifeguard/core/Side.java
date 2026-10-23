package net.xlea99.lifeguard.core;

import java.util.Locale;

/**
 * Who owns an entry's value in multiplayer. A client entry is each player's own choice; a server entry is the server's,
 * synced to clients on join and shown read-only to them. An entry with a part on each side is split into a parent and a
 * child with different sides.
 */
public enum Side {
    CLIENT,
    SERVER;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
