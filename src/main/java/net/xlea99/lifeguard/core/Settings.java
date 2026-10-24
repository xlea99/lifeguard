package net.xlea99.lifeguard.core;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A copy of everything a player can set: a profile per side, a switch per entry, a value per sub-value. Editing it
 * changes nothing live.
 *
 * <p>The menu edits one of these and writes it to the config files; the game picks it up at the next restart. Nothing
 * applies half-live: mixins were decided at startup, so a live change would reach runtime-gated code and not
 * load-gated code. It also remembers the live state while a client follows a remote server, to restore on leaving.
 */
public final class Settings implements SettingsView {
    private final Registry registry;
    private final EnumMap<Side, Profile> profiles = new EnumMap<>(Side.class);
    private final Map<Entry, Switch> switches = new HashMap<>();
    private final Map<SubValue<?>, Object> values = new HashMap<>();

    private Settings(Registry registry) {
        this.registry = registry;
    }

    /** A copy of what this session runs with. */
    public static Settings copyOf(Registry registry) {
        return copyOf(registry, registry);
    }

    public static Settings copyOf(Registry registry, SettingsView view) {
        Settings settings = new Settings(registry);
        for (Side side : Side.values())
            settings.profiles.put(side, view.profile(side));
        for (Entry entry : registry.entries()) {
            settings.switches.put(entry, view.setting(entry));
            for (SubValue<?> value : entry.values())
                settings.values.put(value, view.value(value));
        }
        return settings;
    }

    public Settings copy() {
        return copyOf(registry, this);
    }

    public Registry registry() {
        return registry;
    }

    @Override
    public Profile profile(Side side) {
        return profiles.get(side);
    }

    @Override
    public Switch setting(Entry entry) {
        return switches.get(entry);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T value(SubValue<T> value) {
        return (T) values.get(value);
    }

    public void setProfile(Side side, Profile profile) {
        profiles.put(side, Objects.requireNonNull(profile));
    }

    public void set(Entry entry, Switch setting) {
        switches.put(entry, Objects.requireNonNull(setting));
    }

    public <T> void setValue(SubValue<T> value, T newValue) {
        if (!value.accepts(newValue))
            throw new IllegalArgumentException(value.describe() + " can't be " + newValue + " (" + value.rangeText() + ")");
        values.put(value, newValue);
    }

    /** Sets from a raw value read off disk; returns why it didn't fit, or null when it did. */
    public String setValueFrom(SubValue<?> value, Object raw) {
        SubValue.Parsed<?> parsed = value.parse(raw);
        if (parsed.ok())
            values.put(value, parsed.value());
        return parsed.problem();
    }

    /** Puts one side back to its defaults: the default profile, every switch on auto, every value at its default. */
    public void resetSide(Side side) {
        profiles.put(side, Profile.DEFAULT);
        for (Entry entry : registry.entries()) {
            if (entry.side() != side)
                continue;
            switches.put(entry, Switch.AUTO);
            for (SubValue<?> value : entry.values())
                values.put(value, value.defaultValue());
        }
    }

    /** Makes the live state match this. Only for startup and for leaving a remote server, never from the menu. */
    public void applyTo(Registry target) {
        for (Side side : Side.values())
            target.setProfile(side, profiles.get(side));
        for (Entry entry : target.entries()) {
            entry.set(switches.get(entry));
            for (SubValue<?> value : entry.values())
                applyValue(value);
        }
    }

    private <T> void applyValue(SubValue<T> value) {
        value.set(value(value));
    }

    /** Entries of one side set to On or Off instead of following the profile. */
    public int overrides(Side side) {
        int count = 0;
        for (Entry entry : registry.entries())
            if (entry.side() == side && switches.get(entry) != Switch.AUTO)
                count++;
        return count;
    }

    /** Entries active under this state, counting both sides. */
    public int activeCount() {
        int count = 0;
        for (Entry entry : registry.entries())
            if (entry.activeUnder(this))
                count++;
        return count;
    }

    /** Entries whose answer to "am I on" differs between the two states: what a restart would change. */
    public static List<Entry> changedEntries(SettingsView before, SettingsView after, Registry registry) {
        List<Entry> changed = new ArrayList<>();
        for (Entry entry : registry.entries())
            if (entry.activeUnder(before) != entry.activeUnder(after))
                changed.add(entry);
        return changed;
    }

    /** Whether anything a player can set differs, including switches and values that don't change who's active. */
    public boolean sameAs(SettingsView other) {
        for (Side side : Side.values())
            if (profiles.get(side) != other.profile(side))
                return false;
        for (Entry entry : registry.entries()) {
            if (switches.get(entry) != other.setting(entry))
                return false;
            for (SubValue<?> value : entry.values())
                if (!Objects.equals(values.get(value), other.value(value)))
                    return false;
        }
        return true;
    }
}
