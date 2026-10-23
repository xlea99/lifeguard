package net.xlea99.lifeguard.core;

/**
 * How an entry's mixins follow its switch.
 *
 * <p>Mixins are applied once, when the game starts, from the local config file. That is too early to know any server's
 * settings, so a server-owned entry whose mixins run on the client can't be load-gated: it has to load them always and
 * ask {@link Entry#active()} each time they run.
 */
public enum Gating {
    /** The default: the mixin plugin skips the entry's package when it is inactive at startup. Off = never loaded. */
    LOAD,
    /** The mixins always load and check {@link Entry#active()} on every call. */
    RUNTIME
}
