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
    // These mixin packages hold infrastructure (always on, or shared by groups), so no entry may own one.
    private static final Set<String> RESERVED_IDS = Set.of(MixinGate.CORE_PACKAGE, MixinGate.SHARED_PACKAGE);
    // Every entry id is also a Java package (its mixins live there), so it may not be a keyword.
    private static final Set<String> JAVA_KEYWORDS = Set.of(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
        "default", "do", "double", "else", "enum", "extends", "false", "final", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package", "private",
        "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "true", "try", "void", "volatile", "while", "_");

    private final List<Entry> declared = new ArrayList<>();
    private final Map<String, Entry> byId = new LinkedHashMap<>();
    private final List<Entry> topLevel = new ArrayList<>();
    private final EnumMap<Side, Profile> profiles = new EnumMap<>(Side.class);
    private Predicate<String> modPresent = modId -> false;
    private volatile boolean frozen;
    private volatile int generation;

    public Registry() {
        for (Side side : Side.values())
            profiles.put(side, Profile.DEFAULT);
    }

    // --- declaring -----------------------------------------------------------------------------------------------------

    public Entry feature(String id, Taste taste) {
        return declare(id, EntryClass.FEATURE, taste);
    }

    public Entry tweak(String id, Taste taste) {
        return declare(id, EntryClass.TWEAK, taste);
    }

    public Entry compat(String id, Taste taste) {
        return declare(id, EntryClass.COMPAT, taste);
    }

    public Entry fix(String id, Taste taste) {
        return declare(id, EntryClass.FIX, taste);
    }

    private Entry declare(String id, EntryClass kind, Taste taste) {
        if (frozen)
            throw new IllegalStateException(id + ": declared after startup; every entry must exist before the freeze");
        Entry entry = new Entry(this, id, kind, taste);
        declared.add(entry);
        return entry;
    }

    // --- the freeze ----------------------------------------------------------------------------------------------------

    /**
     * Validates every declaration and locks them. {@code langKeys} are the keys in Lifeguard's {@code en_us.json};
     * every entry needs its name and description there.
     *
     * @throws EntryValidationException listing every problem found, not just the first
     */
    public void freeze(Set<String> langKeys) {
        if (frozen)
            throw new IllegalStateException("the registry is already frozen");
        // An entry's home is read through the entries it's shown under, so a loop there has to be caught before
        // anything asks for one; past this point every chain ends.
        List<String> loops = shownUnderLoops();
        if (!loops.isEmpty())
            throw new EntryValidationException(loops);
        List<String> problems = new ArrayList<>();
        Set<Entry> declaredSet = new HashSet<>(declared);

        for (Entry entry : declared)
            checkDeclaration(entry, declaredSet, problems);
        Map<String, Entry> ids = new LinkedHashMap<>();
        for (Entry entry : declared) {
            Entry clash = ids.putIfAbsent(entry.id(), entry);
            if (clash != null && clash != entry)
                problems.add(entry.id() + ": declared twice");
        }
        checkCycles(declared, problems);
        checkConfigKeys(declared, problems);
        for (Entry entry : declared)
            checkLang(entry, langKeys, problems);

        if (!problems.isEmpty())
            throw new EntryValidationException(problems);

        byId.putAll(ids);
        for (Entry entry : declared) {
            if (entry.shownUnder() == null)
                topLevel.add(entry);
            else
                entry.shownUnder().addShownBelow(entry);
        }
        frozen = true;
        changed();
    }

    private void checkDeclaration(Entry entry, Set<Entry> declaredSet, List<String> problems) {
        String id = entry.id();
        if (!NAME.matcher(id).matches())
            problems.add(id + ": ids are lowercase letters, digits and underscores, starting with a letter");
        if (RESERVED_IDS.contains(id))
            problems.add(id + ": '" + id + "' is reserved for Lifeguard's own mixin packages");
        if (JAVA_KEYWORDS.contains(id))
            problems.add(id + ": '" + id + "' is a Java keyword, and entry ids are mixin package names");
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
        if (!entry.cancelledMixins().isEmpty()) {
            if (entry.restart() != null && entry.restart() != Restart.GAME)
                problems.add(id + ": cancelling other mods' mixins needs a game restart (they're decided at startup)");
            if (entry.gating() != Gating.LOAD)
                problems.add(id + ": entries that cancel other mods' mixins are load-gated (decided once, at startup)");
            if (entry.requiredMods().isEmpty())
                problems.add(id + ": cancels other mods' mixins but requires no mod (name the mods they belong to)");
            if (new HashSet<>(entry.cancelledMixins()).size() != entry.cancelledMixins().size())
                problems.add(id + ": cancels the same mixin twice");
        }
        if (entry.kind() == EntryClass.FIX && entry.requiredMods().size() > 1)
            problems.add(id + ": a fix requires at most one mod; one that needs " + entry.requiredMods()
                + " is compat, or a fix on the mod that is actually broken");

        Entry shownUnder = entry.shownUnder();
        if (shownUnder == null) {
            String home = entry.declaredHome();
            if (home == null)
                problems.add(id + ": no home declared (the menu group: a mod it requires, Entry.LIFEGUARD or Entry.MINECRAFT),"
                    + " and not shown under another entry");
            else if (!home.equals(Entry.LIFEGUARD) && !home.equals(Entry.MINECRAFT) && !entry.requiredMods().contains(home))
                problems.add(id + ": home '" + home + "' isn't a mod it requires, Entry.LIFEGUARD or Entry.MINECRAFT");
        } else {
            if (entry.declaredHome() != null)
                problems.add(id + ": declares a home and is shown under " + shownUnder.id()
                    + "; a nested entry lives in that one's group");
            if (shownUnder == entry)
                problems.add(id + ": is shown under itself");
            else if (shownUnder.registry() != this || !declaredSet.contains(shownUnder))
                problems.add(id + ": is shown under " + shownUnder.id() + ", which belongs to another registry");
        }

        Set<Entry> seen = new HashSet<>();
        for (Entry required : entry.requiredEntries()) {
            if (required == null) {
                problems.add(id + ": requires an entry that is still null (declared later in another class?)");
                continue;
            }
            if (required == entry)
                problems.add(id + ": requires itself");
            if (!seen.add(required))
                problems.add(id + ": requires " + required.id() + " twice");
            if (required.registry() != this || !declaredSet.contains(required))
                problems.add(id + ": requires " + required.id() + " from another registry");
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

    private List<String> shownUnderLoops() {
        List<String> problems = new ArrayList<>();
        for (Entry entry : declared) {
            Set<Entry> chain = new HashSet<>();
            for (Entry up = entry; up != null; up = up.shownUnder()) {
                if (!chain.add(up)) {
                    problems.add(entry.id() + ": the entries it's shown under loop back to " + up.id());
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

    /**
     * An entry depends on everything it requires. A loop through those edges would make {@link Entry#active()} recurse
     * forever, so it is refused here. Each loop is reported once.
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
        for (Entry required : entry.requiredEntries())
            if (required != null)
                visit(required, state, path, problems);
        path.remove(path.size() - 1);
        state.put(entry, 2);
    }

    /** In the config file an entry's table holds {@code enabled} beside its sub-values, so none may take that name. */
    private static void checkConfigKeys(List<Entry> entries, List<String> problems) {
        for (Entry entry : entries)
            for (SubValue<?> value : entry.values())
                if (value.name().equals("enabled"))
                    problems.add(entry.id() + ": a sub-value can't be called 'enabled' (that's the switch's key)");
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

    /** The entries not shown under another: the top of each menu group's tree. */
    public List<Entry> topLevel() {
        requireFrozen();
        return Collections.unmodifiableList(topLevel);
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
