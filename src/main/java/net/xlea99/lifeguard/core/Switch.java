package net.xlea99.lifeguard.core;

import java.util.Locale;
import java.util.Optional;

/** The three-way switch every entry has in the config file. Auto follows the profile; On and Off are overrides. */
public enum Switch {
    AUTO,
    ON,
    OFF;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<Switch> byId(String id) {
        for (Switch value : values())
            if (value.id().equals(id))
                return Optional.of(value);
        return Optional.empty();
    }
}
