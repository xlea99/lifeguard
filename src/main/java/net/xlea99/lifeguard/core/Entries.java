package net.xlea99.lifeguard.core;

/** Where entries are declared: {@code Entries.feature("jukebox_playlists", Taste.LIGHT)} adds to Lifeguard's one registry. */
public final class Entries {
    public static final Registry REGISTRY = new Registry();

    private Entries() {
    }

    public static Entry feature(String id, Taste taste) {
        return REGISTRY.feature(id, taste);
    }

    public static Entry tweak(String id, Taste taste) {
        return REGISTRY.tweak(id, taste);
    }

    public static Entry compat(String id, Taste taste) {
        return REGISTRY.compat(id, taste);
    }

    public static Entry fix(String id, Taste taste) {
        return REGISTRY.fix(id, taste);
    }
}
