package net.xlea99.lifeguard.client.menu;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.resources.language.I18n;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.EntryClass;
import net.xlea99.lifeguard.core.Settings;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Switch;
import net.xlea99.lifeguard.core.Taste;

/**
 * What the filter column has ticked. Within a group any ticked chip matches; across groups all of them must; a group
 * with nothing ticked doesn't filter.
 *
 * <p>Entries needing a mod that isn't installed are left out unless "include missing-mod entries" is ticked: a player
 * can't use them, so in a plain list they're noise. Asking for one directly (ticking a mod it needs, or searching for
 * it) shows it anyway, since asking for something by name and getting nothing back would be worse.
 */
final class Filters {
    enum State { ON, OFF, OVERRIDDEN }

    final Set<EntryClass> classes = EnumSet.noneOf(EntryClass.class);
    final Set<Taste> tastes = EnumSet.noneOf(Taste.class);
    final Set<Side> sides = EnumSet.noneOf(Side.class);
    final Set<State> states = EnumSet.noneOf(State.class);
    final Set<String> mods = new HashSet<>();
    String search = "";
    /** A viewing preference rather than a filter: Clear leaves it alone. */
    boolean includeMissing;

    boolean any() {
        return !classes.isEmpty() || !tastes.isEmpty() || !sides.isEmpty() || !states.isEmpty() || !mods.isEmpty()
            || !search.isBlank();
    }

    void clear() {
        classes.clear();
        tastes.clear();
        sides.clear();
        states.clear();
        mods.clear();
        search = "";
    }

    static <T> void toggle(Set<T> set, T value) {
        if (!set.remove(value))
            set.add(value);
    }

    boolean matches(Entry entry, Settings draft) {
        return matches(entry, draft, includeMissing);
    }

    /** For the checkbox's count: entries the filters would show if missing-mod entries were included. */
    boolean hiddenOnlyByMissingMod(Entry entry, Settings draft) {
        return !matches(entry, draft, includeMissing) && matches(entry, draft, true);
    }

    private boolean matches(Entry entry, Settings draft, boolean showMissing) {
        boolean installed = entry.installed();
        if (!installed && !showMissing && !asksFor(entry))
            return false;
        if (!classes.isEmpty() && !classes.contains(entry.kind()))
            return false;
        if (!tastes.isEmpty() && !tastes.contains(entry.taste()))
            return false;
        if (!sides.isEmpty() && !sides.contains(entry.side()))
            return false;
        if (!mods.isEmpty() && entry.requiredMods().stream().noneMatch(mods::contains))
            return false;
        if (!states.isEmpty() && !matchesState(entry, draft, installed))
            return false;
        return search.isBlank() || matchesSearch(entry);
    }

    /** Whether this entry was asked for directly: a ticked mod it (or one it's listed under) needs, or a search hit. */
    private boolean asksFor(Entry entry) {
        for (Entry at = entry; at != null; at = at.shownUnder())
            if (at.requiredMods().stream().anyMatch(mods::contains))
                return true;
        return !search.isBlank() && matchesSearch(entry);
    }

    private boolean matchesState(Entry entry, Settings draft, boolean installed) {
        boolean active = entry.activeUnder(draft);
        return states.contains(State.ON) && active
            || states.contains(State.OFF) && installed && !active
            || states.contains(State.OVERRIDDEN) && draft.setting(entry) != Switch.AUTO;
    }

    private boolean matchesSearch(Entry entry) {
        String needle = search.toLowerCase(Locale.ROOT).trim();
        return entry.id().contains(needle)
            || I18n.get(entry.nameKey()).toLowerCase(Locale.ROOT).contains(needle)
            || I18n.get(entry.descKey()).toLowerCase(Locale.ROOT).contains(needle);
    }
}
