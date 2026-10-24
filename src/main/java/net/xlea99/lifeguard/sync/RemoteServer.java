package net.xlea99.lifeguard.sync;

import java.util.List;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.ServerSnapshot;
import net.xlea99.lifeguard.core.Settings;
import net.xlea99.lifeguard.core.Side;

/**
 * Whether this client is following a remote server's entry settings right now, instead of its own. The menu reads it
 * to show server entries read-only.
 *
 * <p>Leaving restores the live state from before the join, not the file: the file may hold edits saved from the menu
 * that are waiting for a restart, and loading them now would apply them half-live.
 */
public final class RemoteServer {
    private static volatile Settings beforeJoin;

    private RemoteServer() {
    }

    public static boolean following() {
        return beforeJoin != null;
    }

    static synchronized void follow(ServerSnapshot snapshot) {
        if (beforeJoin == null)
            beforeJoin = Settings.copyOf(Entries.REGISTRY);
        List<String> problems = snapshot.applyTo(Entries.REGISTRY);
        for (String problem : problems)
            Lifeguard.LOGGER.warn("Lifeguard: {}", problem);
        Lifeguard.LOGGER.info("Lifeguard: following the server's settings (profile {}, {} server entries)",
            Entries.REGISTRY.profile(Side.SERVER).id(), snapshot.entries().size());
    }

    static synchronized void stopFollowing() {
        if (beforeJoin == null)
            return;
        beforeJoin.applyTo(Entries.REGISTRY);
        beforeJoin = null;
        Lifeguard.LOGGER.info("Lifeguard: back to this game's own settings");
    }
}
