package net.xlea99.lifeguard.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * One thing the player can switch on or off. Declared once, as a constant, and everything else (the config file, the
 * menu, mixin gating, the mod page) reads from that declaration:
 *
 * <pre>{@code
 * Entry JUKEBOX = Entries.feature("jukebox", Taste.SUBTLE)
 *     .side(Side.SERVER).restart(Restart.GAME);
 * Entry JUKEBOX_SPEAKERS = Entries.compat("speakers", Taste.SUBTLE)
 *     .parent(JUKEBOX).requiresMods("supplementaries")
 *     .side(Side.SERVER).restart(Restart.GAME)
 *     .values(MAX_SPEAKERS);
 * }</pre>
 *
 * <p>Nothing about an entry is implied: side and restart have no defaults, and its words live in the lang file under
 * {@link #nameKey()} and {@link #descKey()}. The declaration methods only work until the registry is frozen at startup;
 * after that the declaration is fixed and only the switch changes.
 */
public final class Entry {
    private final Registry registry;
    private final String name;
    private final EntryClass kind;
    private final Taste taste;

    private Entry parent;
    private Side side;
    private Restart restart;
    private Gating gating = Gating.LOAD;
    private boolean planned;
    private final Set<String> requiredMods = new LinkedHashSet<>();
    private final List<Entry> requiredEntries = new ArrayList<>();
    private final List<SubValue<?>> values = new ArrayList<>();
    private final List<Entry> children = new ArrayList<>();

    private volatile Switch setting = Switch.AUTO;

    // active() is asked from hot paths (runtime-gated mixins), so its answer is cached until the registry's generation
    // moves, which happens whenever a switch, a profile or mod presence changes. The generation is written after the
    // value and read before it, so a reader that sees the new generation also sees the new value.
    private boolean cachedActive;
    private volatile int cachedGeneration = -1;

    Entry(Registry registry, String name, EntryClass kind, Taste taste) {
        this.registry = registry;
        this.name = Objects.requireNonNull(name);
        this.kind = Objects.requireNonNull(kind);
        this.taste = Objects.requireNonNull(taste);
    }

    // --- declaration ---------------------------------------------------------------------------------------------------

    public Entry parent(Entry parent) {
        declaring();
        this.parent = parent;
        return this;
    }

    public Entry side(Side side) {
        declaring();
        this.side = Objects.requireNonNull(side);
        return this;
    }

    public Entry restart(Restart restart) {
        declaring();
        this.restart = Objects.requireNonNull(restart);
        return this;
    }

    /** For a server-owned entry whose mixins run on the client; see {@link Gating#RUNTIME}. */
    public Entry runtimeGated() {
        declaring();
        this.gating = Gating.RUNTIME;
        return this;
    }

    /** Declared ahead of its code (a port not done yet). Visible in development, dropped from release builds. */
    public Entry planned() {
        declaring();
        this.planned = true;
        return this;
    }

    public Entry requiresMods(String... modIds) {
        declaring();
        Collections.addAll(requiredMods, modIds);
        return this;
    }

    /** Entries elsewhere in the tree this one needs. A parent never needs listing: it is required already. */
    public Entry requiresEntries(Entry... entries) {
        declaring();
        // Kept as given, nulls included: a null here means a constant declared later in another class (static init
        // order), and validation names it instead of the declaration silently losing a requirement.
        Collections.addAll(requiredEntries, entries);
        return this;
    }

    public Entry values(SubValue<?>... subValues) {
        declaring();
        for (SubValue<?> value : subValues) {
            value.attach(this);
            values.add(value);
        }
        return this;
    }

    private void declaring() {
        if (registry.frozen())
            throw new IllegalStateException(name + ": entries can't change their declaration after startup");
    }

    // --- what it is ----------------------------------------------------------------------------------------------------

    /** The stable key the config file and lang file use, e.g. {@code jukebox.speakers}. Never renamed once shipped. */
    public String id() {
        return parent == null ? name : parent.id() + "." + name;
    }

    public String name() {
        return name;
    }

    public EntryClass kind() {
        return kind;
    }

    public Taste taste() {
        return taste;
    }

    public Entry parent() {
        return parent;
    }

    public List<Entry> children() {
        return Collections.unmodifiableList(children);
    }

    public Side side() {
        return side;
    }

    public Restart restart() {
        return restart;
    }

    public Gating gating() {
        return gating;
    }

    public boolean isPlanned() {
        return planned;
    }

    public Set<String> requiredMods() {
        return Collections.unmodifiableSet(requiredMods);
    }

    public List<Entry> requiredEntries() {
        return Collections.unmodifiableList(requiredEntries);
    }

    public List<SubValue<?>> values() {
        return Collections.unmodifiableList(values);
    }

    /** {@code lifeguard.entry.<id>}: the entry's display name. */
    public String nameKey() {
        return "lifeguard.entry." + id();
    }

    /** {@code lifeguard.entry.<id>.desc}: what it does, and why each required mod is needed. */
    public String descKey() {
        return nameKey() + ".desc";
    }

    Registry registry() {
        return registry;
    }

    void addChild(Entry child) {
        children.add(child);
    }

    void clearChildren() {
        children.clear();
    }

    // --- the switch ----------------------------------------------------------------------------------------------------

    public Switch setting() {
        return setting;
    }

    public void set(Switch newSetting) {
        if (newSetting != setting) {
            setting = Objects.requireNonNull(newSetting);
            registry.changed();
        }
    }

    /** Whether the switch says on: an override, or Auto with the side's profile reaching this entry's taste. */
    public boolean switchedOn() {
        return switch (setting) {
            case ON -> true;
            case OFF -> false;
            case AUTO -> registry.profile(side).turnsOn(taste);
        };
    }

    /** Required mods that aren't installed. Empty when the entry can run. */
    public List<String> missingMods() {
        List<String> missing = new ArrayList<>();
        for (String modId : requiredMods)
            if (!registry.modPresent(modId))
                missing.add(modId);
        return missing;
    }

    /**
     * Whether everything this entry needs is installed (its mods, its parent's, its required entries'), whatever the
     * switches say. A runtime-gated entry's mixins load whenever this holds.
     */
    public boolean installed() {
        if (!missingMods().isEmpty())
            return false;
        if (parent != null && !parent.installed())
            return false;
        for (Entry required : requiredEntries)
            if (!required.installed())
                return false;
        return true;
    }

    /**
     * The one question code asks. An entry is active when its parent is active (or it has none), every required mod is
     * present, every required entry is active, and its switch is on. Turning a parent off silences its children but keeps
     * their switches, so turning it back on restores them.
     */
    public boolean active() {
        registry.requireFrozen();
        int generation = registry.generation();
        if (cachedGeneration != generation) {
            cachedActive = resolve();
            cachedGeneration = generation;
        }
        return cachedActive;
    }

    private boolean resolve() {
        if (parent != null && !parent.active())
            return false;
        for (String modId : requiredMods)
            if (!registry.modPresent(modId))
                return false;
        for (Entry required : requiredEntries)
            if (!required.active())
                return false;
        return switchedOn();
    }

    @Override
    public String toString() {
        return "Entry[" + id() + "]";
    }
}
