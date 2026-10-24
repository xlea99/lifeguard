package net.xlea99.lifeguard.core;

/** Where entries are declared: {@code Entries.feature("jukebox", Taste.LIGHT)} adds to Lifeguard's one registry. */
public final class Entries {
    public static final Registry REGISTRY = new Registry();

    private Entries() {
    }

    public static Entry feature(String name, Taste taste) {
        return REGISTRY.feature(name, taste);
    }

    public static Entry tweak(String name, Taste taste) {
        return REGISTRY.tweak(name, taste);
    }

    public static Entry compat(String name, Taste taste) {
        return REGISTRY.compat(name, taste);
    }

    public static Entry fix(String name, Taste taste) {
        return REGISTRY.fix(name, taste);
    }
}
