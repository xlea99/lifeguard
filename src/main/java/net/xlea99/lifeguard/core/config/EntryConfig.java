package net.xlea99.lifeguard.core.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.EntryClass;
import net.xlea99.lifeguard.core.Profile;
import net.xlea99.lifeguard.core.Registry;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.SubValue;
import net.xlea99.lifeguard.core.Switch;
import net.xlea99.lifeguard.core.Taste;

/**
 * One side's config file: {@code lifeguard-client.toml} or {@code lifeguard-server.toml}. Both are global, never per
 * world.
 *
 * <p>The file holds the side's profile and one tri-state switch per entry, plus any sub-values. Its text is generated
 * from the declarations every time, comments included, so the comments can't drift from the code; only the values are
 * the player's. Sections are the class, nesting is the tree: a child sits inside its parent's table, under its root's
 * class. Reading looks for an entry under every class section, so re-classing an entry keeps the player's choice.
 *
 * <p>What's left in the file that no entry claims is kept and written back, so a config survives a version that removes
 * or renames something. A file that doesn't parse is never overwritten: defaults are used for that session and the
 * problem is reported, and the file waits for the player to fix it.
 */
public final class EntryConfig {
    static final String PROFILE_KEY = "profile";
    static final String ENABLED_KEY = "enabled";
    private static final int WRAP = 110;

    private final Path file;
    private final Side side;
    private final Registry registry;
    private final Texts texts;

    // Leaves of the file no entry claimed, by table path, in file order. Written back where they were.
    private final Map<List<String>, Map<String, Object>> unknown = new LinkedHashMap<>();
    private boolean broken;

    public EntryConfig(Path file, Side side, Registry registry, Texts texts) {
        this.file = file;
        this.side = side;
        this.registry = registry;
        this.texts = texts;
    }

    public Path file() {
        return file;
    }

    public Side side() {
        return side;
    }

    /** True when the last load found a file it couldn't parse. {@link #save()} then refuses to overwrite it. */
    public boolean broken() {
        return broken;
    }

    // --- reading -------------------------------------------------------------------------------------------------------

    /** Reads the file (if there is one) into the registry: the side's profile, each entry's switch, each sub-value. */
    public List<String> load() throws IOException {
        unknown.clear();
        broken = false;
        resetToDefaults();
        if (!Files.exists(file))
            return List.of();
        String text = Files.readString(file, StandardCharsets.UTF_8);
        // Insertion-ordered maps, so keys this version doesn't know are written back in the order the player left them.
        CommentedConfig root = TomlFormat.newConfig(LinkedHashMap::new);
        try {
            new TomlParser().parse(new StringReader(text), root, ParsingMode.REPLACE);
        } catch (ParsingException e) {
            broken = true;
            return List.of(file.getFileName() + " isn't valid TOML, so Lifeguard is using defaults this session and"
                + " won't touch the file until it's fixed: " + e.getMessage());
        }
        return apply(root);
    }

    private void resetToDefaults() {
        registry.setProfile(side, Profile.DEFAULT);
        for (Entry entry : sideEntries()) {
            entry.set(Switch.AUTO);
            for (SubValue<?> value : entry.values())
                value.reset();
        }
    }

    private List<String> apply(UnmodifiableConfig root) {
        List<String> warnings = new ArrayList<>();
        Set<List<String>> consumed = new HashSet<>();

        Object profileValue = root.getRaw(List.of(PROFILE_KEY));
        if (profileValue != null) {
            consumed.add(List.of(PROFILE_KEY));
            Optional<Profile> profile = profileValue instanceof String id ? Profile.byId(id) : Optional.empty();
            if (profile.isPresent())
                registry.setProfile(side, profile.get());
            else
                warnings.add("profile = " + Toml.value(profileValue) + " isn't a profile; using "
                    + Profile.DEFAULT.id() + ". Pick one of: " + profileIds());
        }

        for (Entry entry : sideEntries())
            readEntry(root, entry, consumed, warnings);

        collectUnknown(root, new ArrayList<>(), consumed);
        for (Map.Entry<List<String>, Map<String, Object>> table : unknown.entrySet())
            for (String key : table.getValue().keySet())
                warnings.add(dotted(table.getKey(), key) + " isn't a setting of this version of Lifeguard; kept as is");
        return warnings;
    }

    private void readEntry(UnmodifiableConfig root, Entry entry, Set<List<String>> consumed, List<String> warnings) {
        // The entry's own section first, then any other, so a re-classed entry keeps the player's choice.
        List<String> found = null;
        UnmodifiableConfig table = null;
        for (List<String> path : candidatePaths(entry)) {
            Object at = get(root, path);
            if (at instanceof UnmodifiableConfig config) {
                found = path;
                table = config;
                break;
            }
        }
        if (table == null)
            return;

        Object enabled = table.getRaw(List.of(ENABLED_KEY));
        if (enabled != null) {
            consumed.add(child(found, ENABLED_KEY));
            Optional<Switch> setting = enabled instanceof String id ? Switch.byId(id) : Optional.empty();
            if (setting.isPresent())
                entry.set(setting.get());
            else
                warnings.add(dotted(found, ENABLED_KEY) + " = " + Toml.value(enabled)
                    + " isn't auto, on or off; using auto");
        }
        for (SubValue<?> value : entry.values()) {
            Object raw = table.getRaw(List.of(value.name()));
            if (raw == null)
                continue;
            consumed.add(child(found, value.name()));
            String problem = value.setFrom(raw);
            if (problem != null)
                warnings.add(dotted(found, value.name()) + " = " + Toml.value(raw) + " " + problem + "; using the default, "
                    + value.defaultValue());
        }
    }

    private void collectUnknown(UnmodifiableConfig table, List<String> path, Set<List<String>> consumed) {
        for (UnmodifiableConfig.Entry entry : table.entrySet()) {
            List<String> at = child(path, entry.getKey());
            Object value = entry.getRawValue();
            if (value instanceof UnmodifiableConfig sub)
                collectUnknown(sub, at, consumed);
            else if (!consumed.contains(at))
                unknown.computeIfAbsent(path, k -> new LinkedHashMap<>()).put(entry.getKey(), value);
        }
    }

    private static Object get(UnmodifiableConfig root, List<String> path) {
        Object at = root;
        for (String part : path) {
            if (!(at instanceof UnmodifiableConfig config))
                return null;
            at = config.getRaw(List.of(part));
        }
        return at;
    }

    // --- writing -------------------------------------------------------------------------------------------------------

    /**
     * Writes the generated text if it differs from what's on disk. Returns whether it wrote. Refuses, returning false,
     * when the last load found the file broken: the player's text is worth more than our defaults.
     */
    public boolean save() throws IOException {
        if (broken)
            return false;
        String text = render();
        if (Files.exists(file) && normalized(Files.readString(file, StandardCharsets.UTF_8)).equals(text))
            return false;
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, text, StandardCharsets.UTF_8);
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
        return true;
    }

    private static String normalized(String text) {
        return text.replace("\r\n", "\n");
    }

    /** The whole file as it should read now. */
    public String render() {
        StringBuilder out = new StringBuilder();
        out.append("# Lifeguard ").append(side.id()).append(" settings").append('\n');
        out.append("# Each entry's \"enabled\" is auto, on or off. Auto follows the profile; on and off override it.").append('\n');
        out.append("# Comments are rewritten from the mod on every start; only the values are yours. Changes apply after a restart.").append('\n');
        out.append('\n');
        out.append("# ").append(profileIds()).append('\n');
        out.append(PROFILE_KEY).append(" = ").append(Toml.string(registry.profile(side).id())).append('\n');
        writeUnknown(out, List.of(), 0);

        Set<List<String>> written = new HashSet<>();
        written.add(List.of());
        for (EntryClass kind : EntryClass.values())
            for (Entry root : registry.roots())
                if (root.kind() == kind)
                    writeTree(out, root, written);

        boolean header = false;
        for (List<String> path : unknown.keySet()) {
            if (written.contains(path))
                continue;
            if (!header) {
                out.append('\n').append("# Not settings of this version of Lifeguard. Kept so a later version can use them.").append('\n');
                header = true;
            }
            out.append('\n').append('[').append(Toml.tablePath(path)).append(']').append('\n');
            writeUnknown(out, path, 1);
        }
        return out.toString();
    }

    private void writeTree(StringBuilder out, Entry entry, Set<List<String>> written) {
        if (entry.side() == side) {
            List<String> path = tablePath(entry);
            int depth = path.size() - 1;
            String indent = "\t".repeat(depth - 1);
            String inner = "\t".repeat(depth);
            out.append('\n').append(indent).append('[').append(Toml.tablePath(path)).append(']').append('\n');
            comment(out, inner, summary(entry));
            for (String line : wrap(texts.lang(entry.descKey())))
                comment(out, inner, line);
            comment(out, inner, autoNote(entry));
            out.append(inner).append(ENABLED_KEY).append(" = ").append(Toml.string(entry.setting().id())).append('\n');
            for (SubValue<?> value : entry.values()) {
                comment(out, inner, texts.lang(value.nameKey()) + range(value) + ", default " + value.defaultValue());
                out.append(inner).append(Toml.key(value.name())).append(" = ").append(Toml.value(value.get())).append('\n');
            }
            writeUnknown(out, path, depth);
            written.add(path);
        }
        // A child on the other side still lives under its parent; it's just written in the other file.
        for (Entry child : entry.children())
            writeTree(out, child, written);
    }

    private void writeUnknown(StringBuilder out, List<String> path, int depth) {
        Map<String, Object> leaves = unknown.get(path);
        if (leaves == null)
            return;
        String indent = "\t".repeat(depth);
        for (Map.Entry<String, Object> leaf : leaves.entrySet()) {
            comment(out, indent, "Not a setting of this version of Lifeguard; kept.");
            out.append(indent).append(Toml.key(leaf.getKey())).append(" = ").append(Toml.value(leaf.getValue())).append('\n');
        }
    }

    private String summary(Entry entry) {
        String needs;
        if (entry.requiredMods().isEmpty()) {
            needs = "needs nothing";
        } else {
            List<String> names = new ArrayList<>();
            for (String modId : entry.requiredMods())
                names.add(texts.modName(modId));
            needs = "needs " + String.join(", ", names);
        }
        return capitalized(entry.kind().id()) + " · " + entry.taste().id().replace('_', ' ') + " · " + needs;
    }

    private String autoNote(Entry entry) {
        String note;
        if (entry.taste() == Taste.DANGEROUS)
            note = "auto = always off: only \"on\" turns a dangerous entry on";
        else
            note = "auto = " + (registry.profile(side).turnsOn(entry.taste()) ? "on" : "off") + " under this profile";
        List<String> missing = entry.missingMods();
        if (!missing.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (String modId : missing)
                names.add(texts.modName(modId));
            note += " (does nothing: " + String.join(", ", names) + (names.size() == 1 ? " isn't" : " aren't") + " installed)";
        }
        if (entry.isPlanned())
            note += " (planned: no code yet)";
        return note;
    }

    private static String range(SubValue<?> value) {
        String range = value.rangeText();
        return range.isEmpty() ? "" : ": " + range;
    }

    private static void comment(StringBuilder out, String indent, String text) {
        out.append(indent).append("# ").append(text).append('\n');
    }

    private static List<String> wrap(String text) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n")) {
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                if (line.length() > 0 && line.length() + 1 + word.length() > WRAP) {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                if (line.length() > 0)
                    line.append(' ');
                line.append(word);
            }
            lines.add(line.toString());
        }
        return lines;
    }

    // --- paths ---------------------------------------------------------------------------------------------------------

    /** The class section a tree lives in: its root's class, plural. */
    static String section(EntryClass kind) {
        return switch (kind) {
            case FEATURE -> "features";
            case TWEAK -> "tweaks";
            case COMPAT -> "compat";
            case FIX -> "fixes";
        };
    }

    private static List<String> tablePath(Entry entry) {
        return pathUnder(section(root(entry).kind()), entry);
    }

    private static List<List<String>> candidatePaths(Entry entry) {
        List<List<String>> paths = new ArrayList<>();
        paths.add(tablePath(entry));
        for (EntryClass kind : EntryClass.values()) {
            List<String> path = pathUnder(section(kind), entry);
            if (!paths.contains(path))
                paths.add(path);
        }
        return paths;
    }

    private static List<String> pathUnder(String section, Entry entry) {
        List<String> path = new ArrayList<>();
        path.add(section);
        path.addAll(List.of(entry.id().split("\\.")));
        return path;
    }

    private static Entry root(Entry entry) {
        Entry at = entry;
        while (at.parent() != null)
            at = at.parent();
        return at;
    }

    private List<Entry> sideEntries() {
        List<Entry> entries = new ArrayList<>();
        for (Entry entry : registry.entries())
            if (entry.side() == side)
                entries.add(entry);
        return entries;
    }

    private static List<String> child(List<String> path, String key) {
        List<String> at = new ArrayList<>(path);
        at.add(key);
        return List.copyOf(at);
    }

    private static String dotted(List<String> path, String key) {
        return path.isEmpty() ? key : Toml.tablePath(path) + "." + Toml.key(key);
    }

    private static String profileIds() {
        List<String> ids = new ArrayList<>();
        for (Profile profile : Profile.values())
            ids.add(profile.id());
        return String.join(" | ", ids);
    }

    private static String capitalized(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
