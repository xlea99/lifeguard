package net.xlea99.lifeguard.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Every declared entry, and the state they resolve against: one profile per side and which mods are installed.
 *
 * <p>Entries add themselves when they're declared. {@link #freeze} then validates the whole tree at once and fails
 * loudly, listing every problem, before anything can ask an entry whether it is active. A declaration that is wrong
 * crashes in development; it never becomes a silent default.
 */
public final class Registry implements SettingsView {
    private static final Pattern NAME = Pattern.compile("[a-z][a-z0-9_]*");
    private static final Pattern RECIPE_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    // A child with one of these names would share a lang key with its parent's description or sub-values.
    private static final Set<String> RESERVED_NAMES = Set.of("desc", "value");
    // The mixin package of that name holds infrastructure that is always on, so no entry may own it.
    private static final Set<String> RESERVED_ROOT_NAMES = Set.of(MixinGate.CORE_PACKAGE);
    // Every entry id is also a Java package (its mixins live there), so no part of it may be a keyword.
    private static final Set<String> JAVA_KEYWORDS = Set.of(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
        "default", "do", "double", "else", "enum", "extends", "false", "final", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package", "private",
        "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "true", "try", "void", "volatile", "while", "_");

    private final List<Entry> declared = new ArrayList<>();
    private final Map<String, Entry> byId = new LinkedHashMap<>();
    private final List<Entry> roots = new ArrayList<>();
    private final EnumMap<Side, Profile> profiles = new EnumMap<>(Side.class);
    private Predicate<String> modPresent = modId -> false;
    private volatile boolean frozen;
    private volatile int generation;

    public Registry() {
        for (Side side : Side.values())
            profiles.put(side, Profile.DEFAULT);
    }

    // --- declaring -----------------------------------------------------------------------------------------------------

    public Entry feature(String name, Taste taste) {
        return declare(name, EntryClass.FEATURE, taste);
    }

    public Entry tweak(String name, Taste taste) {
        return declare(name, EntryClass.TWEAK, taste);
    }

    public Entry compat(String name, Taste taste) {
        return declare(name, EntryClass.COMPAT, taste);
    }

    public Entry fix(String name, Taste taste) {
        return declare(name, EntryClass.FIX, taste);
    }

    private Entry declare(String name, EntryClass kind, Taste taste) {
        if (frozen)
            throw new IllegalStateException(name + ": declared after startup; every entry must exist before the freeze");
        Entry entry = new Entry(this, name, kind, taste);
        declared.add(entry);
        return entry;
    }

    // --- the freeze ----------------------------------------------------------------------------------------------------

    /**
     * Validates every declaration and locks them. {@code langKeys} are the keys in Lifeguard's {@code en_us.json};
     * every entry needs its name and description there. With {@code includePlanned} false (release builds), planned
     * entries are dropped, and nothing real may depend on one.
     *
     * @throws EntryValidationException listing every problem found, not just the first
     */
    public void freeze(Set<String> langKeys, boolean includePlanned) {
        if (frozen)
            throw new IllegalStateException("the registry is already frozen");
        // Ids are built by walking up the parents, so a parent loop has to be caught (by name) before anything asks
        // for an id; past this point every parent chain ends.
        List<String> loops = parentLoops();
        if (!loops.isEmpty())
            throw new EntryValidationException(loops);
        List<String> problems = new ArrayList<>();
        List<Entry> kept = new ArrayList<>();
        for (Entry entry : declared)
            if (includePlanned || !entry.isPlanned())
                kept.add(entry);
        Set<Entry> keptSet = new HashSet<>(kept);

        for (Entry entry : kept)
            checkDeclaration(entry, keptSet, problems);
        Map<String, Entry> ids = new LinkedHashMap<>();
        for (Entry entry : kept) {
            Entry clash = ids.putIfAbsent(entry.id(), entry);
            if (clash != null && clash != entry)
                problems.add(entry.id() + ": declared twice");
        }
        checkCycles(kept, problems);
        checkConfigKeys(kept, problems);
        for (Entry entry : kept)
            checkLang(entry, langKeys, problems);

        if (!problems.isEmpty())
            throw new EntryValidationException(problems);

        byId.putAll(ids);
        for (Entry entry : kept) {
            entry.clearChildren();
            if (entry.parent() == null)
                roots.add(entry);
        }
        for (Entry entry : kept)
            if (entry.parent() != null)
                entry.parent().addChild(entry);
        frozen = true;
        changed();
    }

    private void checkDeclaration(Entry entry, Set<Entry> kept, List<String> problems) {
        String id = entry.id();
        if (!NAME.matcher(entry.name()).matches())
            problems.add(id + ": name must be lowercase letters, digits and underscores, starting with a letter");
        if (entry.parent() != null && RESERVED_NAMES.contains(entry.name()))
            problems.add(id + ": '" + entry.name() + "' is reserved (it would collide with a lang key)");
        if (entry.parent() == null && RESERVED_ROOT_NAMES.contains(entry.name()))
            problems.add(id + ": '" + entry.name() + "' is reserved for Lifeguard's always-on mixins");
        if (JAVA_KEYWORDS.contains(entry.name()))
            problems.add(id + ": '" + entry.name() + "' is a Java keyword, and entry ids are mixin package names");
        if (entry.side() == null)
            problems.add(id + ": no side declared (client or server)");
        if (entry.restart() == null)
            problems.add(id + ": no restart declared (game or world)");
        if (entry.isRegistryShaping()) {
            if (entry.side() != null && entry.side() != Side.SERVER)
                problems.add(id + ": registry-shaping entries are server entries (the server decides; clients must match)");
            if (entry.restart() != null && entry.restart() != Restart.GAME)
                problems.add(id + ": registry-shaping entries need a game restart");
            if (entry.gating() != Gating.LOAD)
                problems.add(id + ": registry-shaping entries are load-gated (what loaded is what the client has)");
        }
        if (!entry.recipeEdits().isEmpty() && entry.side() != null && entry.side() != Side.SERVER)
            problems.add(id + ": only server entries edit recipes (the server decides what's in a datapack)");
        Set<String> editedRecipes = new HashSet<>();
        for (String recipeId : entry.recipeEdits()) {
            if (!RECIPE_ID.matcher(recipeId).matches())
                problems.add(id + ": '" + recipeId + "' isn't a recipe id (namespace:path)");
            if (!editedRecipes.add(recipeId))
                problems.add(id + ": edits " + recipeId + " twice");
        }
        if (entry.kind() == EntryClass.FIX && entry.requiredMods().size() > 1)
            problems.add(id + ": a fix requires at most one mod; one that needs " + entry.requiredMods()
                + " is compat, or a fix on the mod that is actually broken");

        Entry parent = entry.parent();
        if (parent != null) {
            if (parent.registry() != this)
                problems.add(id + ": parent " + parent.id() + " belongs to another registry");
            else if (!kept.contains(parent))
                problems.add(id + ": its parent " + parent.id() + " is planned, so it would vanish from a release build");
        }

        Set<Entry> seen = new HashSet<>();
        for (Entry required : entry.requiredEntries()) {
            if (required == null) {
                problems.add(id + ": requires an entry that is still null (declared later in another class?)");
                continue;
            }
            if (required == entry)
                problems.add(id + ": requires itself");
            else if (isAncestor(required, entry))
                problems.add(id + ": requires its own ancestor " + required.id() + "; a parent never needs listing");
            if (!seen.add(required))
                problems.add(id + ": requires " + required.id() + " twice");
            if (required.registry() != this)
                problems.add(id + ": requires " + required.id() + " from another registry");
            else if (!kept.contains(required))
                problems.add(id + ": requires " + required.id() + ", which is planned and won't exist in a release build");
        }

        Set<String> valueNames = new HashSet<>();
        for (SubValue<?> value : entry.values()) {
            if (!NAME.matcher(value.name()).matches())
                problems.add(id + "." + value.name() + ": sub-value names follow the same rules as entry names");
            if (!valueNames.add(value.name()))
                problems.add(id + ": two sub-values named " + value.name());
            if (!acceptsDefault(value))
                problems.add(id + "." + value.name() + ": default " + value.defaultValue() + " is outside "
                    + value.rangeText());
        }
    }

    private List<String> parentLoops() {
        List<String> problems = new ArrayList<>();
        for (Entry entry : declared) {
            Set<Entry> chain = new HashSet<>();
            for (Entry up = entry; up != null; up = up.parent()) {
                if (!chain.add(up)) {
                    problems.add(entry.name() + ": its parents loop back to " + up.name());
                    break;
                }
            }
        }
        return problems;
    }

    @SuppressWarnings("unchecked")
    private static <T> boolean acceptsDefault(SubValue<T> value) {
        return value.accepts(value.defaultValue());
    }

    private static boolean isAncestor(Entry candidate, Entry entry) {
        for (Entry up = entry.parent(); up != null; up = up.parent())
            if (up == candidate)
                return true;
        return false;
    }

    /**
     * An entry depends on its parent and on everything it requires. A loop through those edges would make
     * {@link Entry#active()} recurse forever, so it is refused here. Each loop is reported once.
     */
    private static void checkCycles(List<Entry> entries, List<String> problems) {
        Map<Entry, Integer> state = new HashMap<>(); // absent = unvisited, 1 = on the current path, 2 = done
        for (Entry entry : entries)
            visit(entry, state, new ArrayList<>(), problems);
    }

    private static void visit(Entry entry, Map<Entry, Integer> state, List<Entry> path, List<String> problems) {
        Integer seen = state.get(entry);
        if (seen != null) {
            if (seen == 1) {
                List<String> loop = new ArrayList<>();
                for (Entry onPath : path.subList(path.indexOf(entry), path.size()))
                    loop.add(onPath.id());
                loop.add(entry.id());
                problems.add("dependency loop: " + String.join(" -> ", loop));
            }
            return;
        }
        state.put(entry, 1);
        path.add(entry);
        if (entry.parent() != null)
            visit(entry.parent(), state, path, problems);
        for (Entry required : entry.requiredEntries())
            if (required != null)
                visit(required, state, path, problems);
        path.remove(path.size() - 1);
        state.put(entry, 2);
    }

    /**
     * In the config file an entry's table holds {@code enabled}, its sub-values, and its children's tables, all as keys of
     * one TOML table, so none of those names may repeat.
     */
    private static void checkConfigKeys(List<Entry> entries, List<String> problems) {
        Map<Entry, Set<String>> childNames = new HashMap<>();
        for (Entry entry : entries)
            if (entry.parent() != null)
                childNames.computeIfAbsent(entry.parent(), parent -> new HashSet<>()).add(entry.name());
        for (Entry entry : entries) {
            Set<String> children = childNames.getOrDefault(entry, Set.of());
            for (SubValue<?> value : entry.values()) {
                if (value.name().equals("enabled"))
                    problems.add(entry.id() + ": a sub-value can't be called 'enabled' (that's the switch's key)");
                if (children.contains(value.name()))
                    problems.add(entry.id() + ": sub-value '" + value.name() + "' has the same name as a child entry");
            }
        }
    }

    private static void checkLang(Entry entry, Set<String> langKeys, List<String> problems) {
        if (!langKeys.contains(entry.nameKey()))
            problems.add(entry.id() + ": no name in en_us.json (" + entry.nameKey() + ")");
        if (!langKeys.contains(entry.descKey()))
            problems.add(entry.id() + ": no description in en_us.json (" + entry.descKey() + ")");
        for (SubValue<?> value : entry.values())
            if (!langKeys.contains(value.nameKey()))
                problems.add(entry.id() + "." + value.name() + ": no name in en_us.json (" + value.nameKey() + ")");
        // The menu groups entries under their mods' names, falling back to the bare mod id ("scholar").
        for (String modId : entry.requiredMods())
            if (!langKeys.contains("lifeguard.mod." + modId))
                problems.add(entry.id() + ": no name for required mod " + modId + " in en_us.json (lifeguard.mod." + modId + ")");
    }

    // --- reading -------------------------------------------------------------------------------------------------------

    public boolean frozen() {
        return frozen;
    }

    void requireFrozen() {
        if (!frozen)
            throw new IllegalStateException("entries can't be resolved before the registry is validated and frozen");
    }

    /** Every entry, in declaration order. */
    public List<Entry> entries() {
        requireFrozen();
        return List.copyOf(byId.values());
    }

    public List<Entry> roots() {
        requireFrozen();
        return Collections.unmodifiableList(roots);
    }

    public Optional<Entry> get(String id) {
        requireFrozen();
        return Optional.ofNullable(byId.get(id));
    }

    // --- the state entries resolve against -----------------------------------------------------------------------------

    /** The live switch: the registry is the view of what this session runs with. */
    @Override
    public Switch setting(Entry entry) {
        return entry.setting();
    }

    @Override
    public <T> T value(SubValue<T> value) {
        return value.get();
    }

    @Override
    public Profile profile(Side side) {
        return profiles.get(side);
    }

    public void setProfile(Side side, Profile profile) {
        if (profiles.put(side, profile) != profile)
            changed();
    }

    /** Which mods are installed. Set once at startup from the mod loader; tests pass a set. */
    public void setModPresence(Predicate<String> modPresent) {
        this.modPresent = modPresent;
        changed();
    }

    boolean modPresent(String modId) {
        return modPresent.test(modId);
    }

    int generation() {
        return generation;
    }

    void changed() {
        generation++;
    }
}
