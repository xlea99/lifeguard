package net.xlea99.lifeguard.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * One thing the player can switch on or off. Declared once, as a constant, and everything else (the config file, the
 * menu, mixin gating, the mod page) reads from that declaration:
 *
 * <pre>{@code
 * Entry JUKEBOX = Entries.feature("jukebox", Taste.LIGHT)
 *     .side(Side.SERVER).restart(Restart.GAME);
 * Entry JUKEBOX_SPEAKERS = Entries.compat("speakers", Taste.LIGHT)
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
    private boolean registryShaping;
    private boolean resourcePack;
    private boolean ownContent;
    private final Map<String, Set<String>> sharedMixins = new LinkedHashMap<>();
    private final List<String> recipeEdits = new ArrayList<>();
    private final List<String> cancelledMixins = new ArrayList<>();
    private BooleanSupplier cancelWhen = () -> false;
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

    /**
     * Changes the shape of the game's registries (a block's states, say), so a client and the server it joins must
     * agree on it: a mismatched client is turned away at login with a message saying what to match. Only a load-gated
     * server entry that needs a game restart can be one.
     */
    public Entry registryShaping() {
        declaring();
        this.registryShaping = true;
        return this;
    }

    /**
     * Ships client assets in the jar's {@code resourcepacks/<id>} folder, loaded as a required pack (on top, can't be
     * turned off or moved) whenever the entry is active when the game starts.
     */
    public Entry resourcePack() {
        declaring();
        this.resourcePack = true;
        return this;
    }

    /**
     * Edits other mods' recipes, by id ({@code neapolitan:milk_bottle}), while the entry is active. Each ships
     * {@code recipe_edits/<entry id>/<namespace>/<path>.json} holding the recipe's {@code original} JSON and its
     * {@code replacement}. A recipe is only replaced while it is still exactly the original: one a datapack (or a mod
     * update) changed or removed is left alone, so datapacks keep the last word.
     */
    public Entry editsRecipe(String... recipeIds) {
        declaring();
        Collections.addAll(recipeEdits, recipeIds);
        return this;
    }

    /**
     * Switches other mods' mixins off, by class name ({@code com.example.mixin.FooMixin}), when the game starts with
     * this entry active and {@code when} agrees (MixinSquared's canceller, bundled). For an overlap where one mod's
     * mixin replaces the specialist's own code. {@code when} runs before the game's classes load: it may read other
     * mods' config files from disk, nothing more.
     */
    public Entry cancelsMixins(BooleanSupplier when, String... mixinClassNames) {
        declaring();
        this.cancelWhen = Objects.requireNonNull(when);
        Collections.addAll(cancelledMixins, mixinClassNames);
        return this;
    }

    /**
     * Uses mixins shared with other entries, in {@code mixin.shared.<group>}: one hook several entries' content needs
     * (our neodymium blocks being Alex's Caves magnets, say). They're applied when any entry naming the group would have
     * its own applied and the mods it names for the group (the ones the mixins target) are installed, so they check
     * what's active as they run.
     */
    public Entry sharesMixins(String group, String... withMods) {
        declaring();
        sharedMixins.put(group, Set.of(withMods));
        return this;
    }

    /** Lifeguard's own content (no other mod's idea it extends): listed under the menu's Lifeguard group, at the top. */
    public Entry ownContent() {
        declaring();
        this.ownContent = true;
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

    public boolean isRegistryShaping() {
        return registryShaping;
    }

    public boolean hasResourcePack() {
        return resourcePack;
    }

    /** The shared mixin groups this entry uses, each with the mods it needs installed. */
    public Map<String, Set<String>> sharedMixins() {
        return Collections.unmodifiableMap(sharedMixins);
    }

    public boolean isOwnContent() {
        return ownContent;
    }

    /** {@code resourcepacks/<id>}: where the entry's pack lives in the jar, if it has one. */
    public String resourcePackPath() {
        return "resourcepacks/" + id();
    }

    public List<String> recipeEdits() {
        return Collections.unmodifiableList(recipeEdits);
    }

    /** {@code recipe_edits/<id>/<namespace>/<path>.json}: where the jar holds one recipe edit of this entry's. */
    public List<String> cancelledMixins() {
        return Collections.unmodifiableList(cancelledMixins);
    }

    /** Whether this entry switches {@code mixinClassName} off this session: decided once, at startup. */
    public boolean cancels(String mixinClassName) {
        return cancelledMixins.contains(mixinClassName) && active() && cancelWhen.getAsBoolean();
    }

    public String recipeEditPath(String recipeId) {
        int colon = recipeId.indexOf(':');
        return "recipe_edits/" + id() + "/" + recipeId.substring(0, colon) + "/" + recipeId.substring(colon + 1) + ".json";
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

    /** Whether the live switch says on: an override, or Auto with the side's profile reaching this entry's taste. */
    public boolean switchedOn() {
        return switchedOnUnder(registry);
    }

    /** The same question against any state, e.g. a draft the menu is editing. */
    public boolean switchedOnUnder(SettingsView view) {
        return switch (view.setting(this)) {
            case ON -> true;
            case OFF -> false;
            case AUTO -> view.profile(side).turnsOn(taste);
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
            cachedActive = activeUnder(registry);
            cachedGeneration = generation;
        }
        return cachedActive;
    }

    /**
     * The rule behind {@link #active()}, against any state: the live registry, or a draft. There is only this one copy
     * of it, so what the menu predicts is exactly what the next restart will do.
     */
    public boolean activeUnder(SettingsView view) {
        registry.requireFrozen();
        if (parent != null && !parent.activeUnder(view))
            return false;
        for (String modId : requiredMods)
            if (!registry.modPresent(modId))
                return false;
        for (Entry required : requiredEntries)
            if (!required.activeUnder(view))
                return false;
        return switchedOnUnder(view);
    }

    @Override
    public String toString() {
        return "Entry[" + id() + "]";
    }
}
