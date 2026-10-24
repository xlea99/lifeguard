package net.xlea99.lifeguard.core;

import java.util.Locale;

/**
 * How much of our own opinion an entry carries, from none to dangerous. The order matters: a {@link Profile} turns on
 * every entry at or below its ceiling, and no profile's ceiling reaches {@link #DANGEROUS}, so a dangerous entry is
 * only ever on when its switch is explicitly set to On.
 */
public enum Taste {
    /** A direct extension of someone else's precedent; we made no real calls. */
    NONE,
    /** A call was needed, and it deliberately follows an existing precedent. */
    LIGHT,
    /** Our own expression, with no precedent behind it or picking one precedent against others. */
    STRONG,
    /** Only makes sense inside Deep End: it carries the pack's vision, and most players won't want it. */
    DEEP_END,
    /** An escape hatch that hides errors or changes failure behavior. Above every profile's ceiling. */
    DANGEROUS;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
