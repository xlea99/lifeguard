package net.xlea99.lifeguard.client.menu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.resources.language.I18n;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.boot.Boot;
import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Profile;
import net.xlea99.lifeguard.core.Registry;
import net.xlea99.lifeguard.core.Settings;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.SubValue;
import net.xlea99.lifeguard.core.Switch;
import net.xlea99.lifeguard.core.config.EntryConfig;
import net.xlea99.lifeguard.sync.RemoteServer;

/**
 * What the menu is editing, and its rules. Everything here works on a draft: nothing live changes until the next
 * restart, because mixins were decided at startup and a live change would only reach half of them.
 */
final class MenuModel {
    /** The mod "needs no other mod" entries are grouped under. */
    static final String MINECRAFT = "";

    /**
     * One line of the list: a mod heading (its id, MINECRAFT for entries needing no other mod), or an entry at some depth
     * in its tree. A context line is an ancestor shown only because something under it matches the filters.
     */
    record Line(String headingMod, int count, Entry entry, int depth, boolean context) {
        boolean isHeading() {
            return headingMod != null;
        }
    }

    private final Registry registry = Entries.REGISTRY;
    private final Settings draft;
    private final Set<Entry> collapsed = new HashSet<>();
    private final Set<String> collapsedMods = new HashSet<>();
    private final boolean following;
    private final boolean serverBroken;
    private final boolean clientBroken;

    MenuModel() {
        following = RemoteServer.following();
        draft = Settings.copyOf(registry);
        // The files can hold edits saved earlier this session that wait for a restart: the menu shows those. On a
        // remote server the server side stays as the server sent it, since the player can't change it there.
        if (!following)
            read(Boot.serverConfig());
        Boot.clientConfig().ifPresent(this::read);
        serverBroken = !following && Boot.serverConfig().broken();
        clientBroken = Boot.clientConfig().map(EntryConfig::broken).orElse(true);
        // The list is long: it opens with every mod group closed.
        collapseAllMods();
    }

    private void read(EntryConfig config) {
        try {
            for (String warning : config.readInto(draft))
                Lifeguard.LOGGER.warn("Lifeguard {}: {}", config.file().getFileName(), warning);
        } catch (IOException e) {
            Lifeguard.LOGGER.error("Lifeguard couldn't read {}", config.file().getFileName(), e);
        }
    }

    Settings draft() {
        return draft;
    }

    boolean following() {
        return following;
    }

    // --- what can be changed -------------------------------------------------------------------------------------------

    boolean locked(Entry entry) {
        return entry.side() == Side.SERVER ? following || serverBroken : clientBroken;
    }

    /** Why a side can't be edited, or empty when it can. */
    Optional<String> brokenFile(Side side) {
        if (side == Side.SERVER && serverBroken)
            return Optional.of("lifeguard-server.toml");
        if (side == Side.CLIENT && clientBroken)
            return Optional.of("lifeguard-client.toml");
        return Optional.empty();
    }

    /** The switch one step on: Auto, then On, then Off, then back to Auto. Backwards walks the same ring the other way. */
    static Switch step(Switch setting, boolean forward) {
        return switch (setting) {
            case AUTO -> forward ? Switch.ON : Switch.OFF;
            case ON -> forward ? Switch.OFF : Switch.AUTO;
            case OFF -> forward ? Switch.AUTO : Switch.ON;
        };
    }

    void set(Entry entry, Switch setting) {
        if (!locked(entry))
            draft.set(entry, setting);
    }

    // --- profiles ------------------------------------------------------------------------------------------------------

    /** The profile the bar shows as picked, or empty when the two sides differ (a hand-edited file). */
    Optional<Profile> shownProfile() {
        Profile client = draft.profile(Side.CLIENT);
        if (following)
            return Optional.of(client);
        return client == draft.profile(Side.SERVER) ? Optional.of(client) : Optional.empty();
    }

    /** In singleplayer the bar sets both sides; on a remote server, only the player's own. */
    void pickProfile(Profile profile) {
        for (Side side : Side.values())
            if (canEditProfile(side))
                draft.setProfile(side, profile);
    }

    private boolean canEditProfile(Side side) {
        return side == Side.SERVER ? !following && !serverBroken : !clientBroken;
    }

    // --- the status line -----------------------------------------------------------------------------------------------

    int activeCount() {
        return draft.activeCount();
    }

    int overrides() {
        int count = draft.overrides(Side.CLIENT);
        if (!following)
            count += draft.overrides(Side.SERVER);
        return count;
    }

    /** Whether the saved state differs from what this session is running: a restart would change something. */
    boolean restartPending() {
        return !draft.sameAs(registry);
    }

    // --- overrides -----------------------------------------------------------------------------------------------------

    /** Overrides the player could reset: switched On or Off by hand, on a side they can edit. */
    int resettableOverrides() {
        int count = 0;
        for (Entry entry : registry.entries())
            if (!locked(entry) && draft.setting(entry) != Switch.AUTO)
                count++;
        return count;
    }

    /** Every editable switch back to Auto. Sub-values stay: they aren't overrides of the profile. */
    void resetOverrides() {
        for (Entry entry : registry.entries())
            if (!locked(entry))
                draft.set(entry, Switch.AUTO);
    }

    /** What picking a profile would switch on, in list order: for the confirmation Off the Deep End asks for. */
    List<Entry> wouldTurnOn(Profile profile) {
        Settings preview = draft.copy();
        for (Side side : Side.values())
            if (canEditProfile(side))
                preview.setProfile(side, profile);
        List<Entry> turningOn = new ArrayList<>();
        for (Entry entry : Settings.changedEntries(draft, preview, registry))
            if (entry.activeUnder(preview))
                turningOn.add(entry);
        return turningOn;
    }

    // --- the restart summary -------------------------------------------------------------------------------------------

    record ValueChange(Entry entry, SubValue<?> value, Object before, Object after) {
    }

    /** What a restart would change: this session's live state against the draft about to be saved. */
    record Summary(List<Side> profileSides, List<Entry> turningOn, List<Entry> turningOff, List<ValueChange> values) {
        boolean empty() {
            return profileSides.isEmpty() && turningOn.isEmpty() && turningOff.isEmpty() && values.isEmpty();
        }
    }

    Summary summary() {
        List<Side> sides = new ArrayList<>();
        for (Side side : Side.values())
            if (registry.profile(side) != draft.profile(side))
                sides.add(side);
        List<Entry> on = new ArrayList<>();
        List<Entry> off = new ArrayList<>();
        for (Entry entry : Settings.changedEntries(registry, draft, registry))
            (entry.activeUnder(draft) ? on : off).add(entry);
        List<ValueChange> values = new ArrayList<>();
        for (Entry entry : registry.entries())
            for (SubValue<?> value : entry.values())
                if (!Objects.equals(value.get(), draft.value(value)))
                    values.add(new ValueChange(entry, value, value.get(), draft.value(value)));
        return new Summary(sides, on, off, values);
    }

    Profile liveProfile(Side side) {
        return registry.profile(side);
    }

    // --- the list ------------------------------------------------------------------------------------------------------

    boolean collapsed(Entry entry) {
        return collapsed.contains(entry);
    }

    boolean modCollapsed(String mod) {
        return collapsedMods.contains(mod);
    }

    void collapseAllMods() {
        for (Entry root : registry.roots())
            collapsedMods.add(homeMod(root));
    }

    void expandAllMods() {
        collapsedMods.clear();
    }

    void toggleModCollapsed(String mod) {
        if (!collapsedMods.remove(mod))
            collapsedMods.add(mod);
    }

    /** Opens every collapsed ancestor and the entry's mod group, so the entry has a row to scroll to. */
    void expandTo(Entry entry) {
        for (Entry up = entry.parent(); up != null; up = up.parent())
            collapsed.remove(up);
        collapsedMods.remove(homeMod(rootOf(entry)));
    }

    /** Entries elsewhere in the tree that list this one in their requirements. */
    List<Entry> requiredBy(Entry entry) {
        List<Entry> dependents = new ArrayList<>();
        for (Entry other : registry.entries())
            if (other.requiredEntries().contains(entry))
                dependents.add(other);
        return dependents;
    }

    /** Why an entry is on or off under the draft, first reason first: the same order the rule checks them in. */
    Reason why(Entry entry) {
        if (entry.parent() != null && !entry.parent().activeUnder(draft))
            return new Reason(false, Reason.Kind.PARENT_OFF, entry.parent());
        if (!entry.missingMods().isEmpty())
            return new Reason(false, Reason.Kind.MISSING_MODS, null);
        for (Entry required : entry.requiredEntries())
            if (!required.activeUnder(draft))
                return new Reason(false, Reason.Kind.REQUIRED_OFF, required);
        boolean on = entry.switchedOnUnder(draft);
        return new Reason(on, draft.setting(entry) == Switch.AUTO ? Reason.Kind.PROFILE : Reason.Kind.OVERRIDE, null);
    }

    record Reason(boolean active, Kind kind, Entry because) {
        enum Kind { PARENT_OFF, MISSING_MODS, REQUIRED_OFF, PROFILE, OVERRIDE }
    }

    void toggleCollapsed(Entry entry) {
        if (!collapsed.remove(entry))
            collapsed.add(entry);
    }

    /**
     * Headings by mod, each tree under its root's home mod (the first mod its root requires; Minecraft for none), so a
     * tree is never split across headings. Minecraft first, then mods by name; inside each, Feature, Tweak, Compat, Fix,
     * then declaration order.
     *
     * <p>Only entries the filters let through, plus their ancestors greyed as context, so nothing shows without where it
     * lives. A collapsed mod group keeps its heading and nothing under it. While any filter is on, collapsing (entries
     * and groups alike) is ignored: a match is never hidden inside a closed parent.
     */
    List<Line> lines(Filters filters) {
        Set<Entry> matching = matching(filters);
        Set<Entry> shown = new HashSet<>(matching);
        for (Entry entry : matching)
            for (Entry up = entry.parent(); up != null; up = up.parent())
                shown.add(up);
        boolean ignoreCollapse = filters.any();

        Map<String, List<Entry>> rootsByMod = new HashMap<>();
        for (Entry root : registry.roots())
            rootsByMod.computeIfAbsent(homeMod(root), mod -> new ArrayList<>()).add(root);
        List<String> mods = new ArrayList<>(rootsByMod.keySet());
        mods.sort(Comparator.comparing((String mod) -> !mod.equals(MINECRAFT))
            .thenComparing(mod -> modName(mod).toLowerCase(Locale.ROOT)));

        List<Line> lines = new ArrayList<>();
        for (String mod : mods) {
            List<Entry> roots = new ArrayList<>(rootsByMod.get(mod));
            roots.sort(Comparator.comparing(Entry::kind)); // stable: declaration order within a class
            List<Line> section = new ArrayList<>();
            for (Entry root : roots)
                addTree(section, root, 0, shown, matching, ignoreCollapse);
            if (section.isEmpty())
                continue;
            int count = 0;
            for (Entry entry : matching)
                if (homeMod(rootOf(entry)).equals(mod))
                    count++;
            lines.add(new Line(mod, count, null, 0, false));
            if (ignoreCollapse || !collapsedMods.contains(mod))
                lines.addAll(section);
        }
        return lines;
    }

    /** Where a root's tree is listed: the first mod it requires (the one it extends), or Minecraft if none. */
    static String homeMod(Entry root) {
        return root.requiredMods().isEmpty() ? MINECRAFT : root.requiredMods().iterator().next();
    }

    /** A mod's display name, from our lang file; Minecraft for the no-mod group; the id as a last resort. */
    static String modName(String modId) {
        if (modId.equals(MINECRAFT))
            return I18n.get("lifeguard.menu.group.minecraft");
        String key = "lifeguard.mod." + modId;
        return I18n.exists(key) ? I18n.get(key) : modId;
    }

    Set<Entry> matching(Filters filters) {
        Set<Entry> matching = new HashSet<>();
        for (Entry entry : registry.entries())
            if (filters.matches(entry, draft))
                matching.add(entry);
        return matching;
    }

    int totalEntries() {
        return registry.entries().size();
    }

    /** Entries the current filters would show if not for a mod that isn't installed. */
    int hiddenByMissingMods(Filters filters) {
        int count = 0;
        for (Entry entry : registry.entries())
            if (filters.hiddenOnlyByMissingMod(entry, draft))
                count++;
        return count;
    }

    /** Whether a mod is installed, for dimming the mod chips of ones that aren't. */
    boolean modInstalled(String modId) {
        return registry.entries().stream()
            .filter(entry -> entry.requiredMods().contains(modId))
            .findFirst()
            .map(entry -> !entry.missingMods().contains(modId))
            .orElse(true);
    }

    /** Every mod some entry requires, for the mod filter. */
    List<String> referencedMods() {
        Set<String> mods = new HashSet<>();
        for (Entry entry : registry.entries())
            mods.addAll(entry.requiredMods());
        List<String> sorted = new ArrayList<>(mods);
        sorted.sort(Comparator.comparing(mod -> modName(mod).toLowerCase(Locale.ROOT)));
        return sorted;
    }

    int entriesRequiring(String modId) {
        int count = 0;
        for (Entry entry : registry.entries())
            if (entry.requiredMods().contains(modId))
                count++;
        return count;
    }

    private static Entry rootOf(Entry entry) {
        Entry at = entry;
        while (at.parent() != null)
            at = at.parent();
        return at;
    }

    private void addTree(List<Line> lines, Entry entry, int depth, Set<Entry> shown, Set<Entry> matching,
                         boolean ignoreCollapse) {
        if (!shown.contains(entry))
            return;
        lines.add(new Line(null, 0, entry, depth, !matching.contains(entry)));
        if (!ignoreCollapse && collapsed.contains(entry))
            return;
        for (Entry child : entry.children())
            addTree(lines, child, depth + 1, shown, matching, ignoreCollapse);
    }

    // --- saving --------------------------------------------------------------------------------------------------------

    /** Writes the draft to the files it belongs in. Returns a problem to show, or empty when it saved. */
    Optional<String> save() {
        try {
            Optional<EntryConfig> client = Boot.clientConfig();
            if (client.isPresent() && !clientBroken)
                client.get().save(draft);
            if (!following && !serverBroken)
                Boot.serverConfig().save(draft);
            return Optional.empty();
        } catch (IOException e) {
            Lifeguard.LOGGER.error("Lifeguard couldn't save its settings", e);
            return Optional.of(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }
}
