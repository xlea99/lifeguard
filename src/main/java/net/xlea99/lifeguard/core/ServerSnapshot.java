package net.xlea99.lifeguard.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The server's half of the registry as one value: its profile, and every server entry's switch and sub-values.
 *
 * <p>A server sends this to each client as it joins. The client applies it over its own server-side state, so the
 * menu shows what the server actually enforces and runtime-gated code follows the server instead of the player's local
 * file. Client entries are never part of it: those are each player's own.
 */
public record ServerSnapshot(String profile, Map<String, EntryState> entries) {

    /** One entry as the server has it. Sub-values are Integer, Double or Boolean. */
    public record EntryState(Switch setting, Map<String, Object> values) {
    }

    public static ServerSnapshot capture(Registry registry) {
        Map<String, EntryState> entries = new LinkedHashMap<>();
        for (Entry entry : registry.entries()) {
            if (entry.side() != Side.SERVER)
                continue;
            Map<String, Object> values = new LinkedHashMap<>();
            for (SubValue<?> value : entry.values())
                values.put(value.name(), value.get());
            entries.put(entry.id(), new EntryState(entry.setting(), values));
        }
        return new ServerSnapshot(registry.profile(Side.SERVER).id(), entries);
    }

    /**
     * Makes this registry's server side match the snapshot, and returns what didn't fit.
     *
     * <ul>
     *     <li>A server entry the server didn't send is switched off: the server isn't running it (an older Lifeguard),
     *     so nothing of it runs here either.</li>
     *     <li>An id this client doesn't know is skipped: the server is newer, and that entry has no code here.</li>
     *     <li>A sub-value that doesn't fit this client's declaration (a newer range, a different type) falls back to its
     *     default.</li>
     * </ul>
     */
    public List<String> applyTo(Registry registry) {
        List<String> problems = new ArrayList<>();
        Optional<Profile> serverProfile = Profile.byId(profile);
        if (serverProfile.isEmpty())
            problems.add("the server's profile '" + profile + "' isn't one this version knows; using " + Profile.DEFAULT.id());
        registry.setProfile(Side.SERVER, serverProfile.orElse(Profile.DEFAULT));

        for (Entry entry : registry.entries()) {
            if (entry.side() != Side.SERVER)
                continue;
            EntryState state = entries.get(entry.id());
            if (state == null) {
                entry.set(Switch.OFF);
                for (SubValue<?> value : entry.values())
                    value.reset();
                continue;
            }
            entry.set(state.setting());
            for (SubValue<?> value : entry.values()) {
                Object raw = state.values().get(value.name());
                if (raw == null) {
                    value.reset();
                    continue;
                }
                String problem = value.setFrom(raw);
                if (problem != null) {
                    value.reset();
                    problems.add(entry.id() + "." + value.name() + " from the server " + problem + "; using the default");
                }
            }
        }
        for (String id : entries.keySet())
            if (registry.get(id).filter(entry -> entry.side() == Side.SERVER).isEmpty())
                problems.add("the server has an entry '" + id + "' this version doesn't; it does nothing here");
        return problems;
    }
}
