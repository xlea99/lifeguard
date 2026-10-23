package net.xlea99.lifeguard.sync;

import java.io.IOException;
import java.util.List;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.boot.Boot;
import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.ServerSnapshot;
import net.xlea99.lifeguard.core.Side;

/**
 * Whether this client is following a remote server's entry settings right now, instead of its own server file.
 * The menu reads it to show server entries read-only.
 */
public final class RemoteServer {
    private static volatile boolean following;

    private RemoteServer() {
    }

    public static boolean following() {
        return following;
    }

    static void follow(ServerSnapshot snapshot) {
        List<String> problems = snapshot.applyTo(Entries.REGISTRY);
        for (String problem : problems)
            Lifeguard.LOGGER.warn("Lifeguard: {}", problem);
        following = true;
        Lifeguard.LOGGER.info("Lifeguard: following the server's settings (profile {}, {} server entries)",
            Entries.REGISTRY.profile(Side.SERVER).id(), snapshot.entries().size());
    }

    /** Back to the local server file, read fresh from disk. */
    static void stopFollowing() {
        if (!following)
            return;
        following = false;
        try {
            for (String warning : Boot.serverConfig().load())
                Lifeguard.LOGGER.warn("Lifeguard lifeguard-server.toml: {}", warning);
            Lifeguard.LOGGER.info("Lifeguard: back to the local server settings");
        } catch (IOException e) {
            Lifeguard.LOGGER.error("Lifeguard couldn't reread lifeguard-server.toml; server entries are on defaults", e);
        }
    }
}
