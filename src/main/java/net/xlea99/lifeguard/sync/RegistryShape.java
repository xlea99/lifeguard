package net.xlea99.lifeguard.sync;

/**
 * Whether the server this client is joining told it which registry-shaping entries it loaded. A server without
 * Lifeguard, or with one from before the check, never says; a client that loaded one can't safely play there.
 */
final class RegistryShape {
    private static volatile boolean received;

    private RegistryShape() {
    }

    static void received() {
        received = true;
    }

    /** Read once, at play login; the next join starts unanswered again. */
    static boolean takeReceived() {
        boolean was = received;
        received = false;
        return was;
    }
}
