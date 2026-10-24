package net.xlea99.lifeguard.core;

import java.util.Locale;
import java.util.Optional;

/** A taste ceiling: a profile switches on every entry whose taste is at or below it. Tasteful is the default. */
public enum Profile {
    TASTELESS(Taste.NONE),
    TASTEFUL(Taste.LIGHT),
    FLAVOR_EXPLOSION(Taste.STRONG),
    // The highest ceiling is deep_end, not dangerous: no profile ever turns a dangerous entry on.
    OFF_THE_DEEP_END(Taste.DEEP_END);

    public static final Profile DEFAULT = TASTEFUL;

    private final Taste ceiling;

    Profile(Taste ceiling) {
        this.ceiling = ceiling;
    }

    public Taste ceiling() {
        return ceiling;
    }

    public boolean turnsOn(Taste taste) {
        return taste.compareTo(ceiling) <= 0;
    }

    /** The id the config file uses, e.g. {@code flavor_explosion}. */
    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<Profile> byId(String id) {
        for (Profile profile : values())
            if (profile.id().equals(id))
                return Optional.of(profile);
        return Optional.empty();
    }
}
