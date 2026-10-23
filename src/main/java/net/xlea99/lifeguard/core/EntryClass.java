package net.xlea99.lifeguard.core;

import java.util.Locale;

/** What kind of thing an entry is, judged by its relationship to the rest of the pack, never by how much it adds. */
public enum EntryClass {
    /** New content or behavior we choose to add, that no existing mod in the pack describes. */
    FEATURE,
    /** An adjustment to something that already exists, that data can't make. Removing content is a tweak too. */
    TWEAK,
    /** Makes two things that each work alone speak to each other better. */
    COMPAT,
    /** Corrects something wrong in ONE mod, or in vanilla. Requires at most one mod. */
    FIX;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
