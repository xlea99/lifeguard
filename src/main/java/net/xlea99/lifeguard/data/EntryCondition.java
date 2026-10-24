package net.xlea99.lifeguard.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Side;

/**
 * {@code lifeguard:entry}: data (recipes, loot, advancements) that exists only while an entry is active, for example
 * <pre>{"type": "lifeguard:entry", "entry": "dye_depot_runes", "and": [{"type": "zeta:flag", "flag": "color_runes"}]}</pre>
 *
 * <p>{@code and} holds conditions that belong to the entry's own mods. NeoForge decodes a whole condition list before
 * testing any of it, so a {@code zeta:flag} beside this one would fail to parse (and log an error per file) wherever
 * Zeta isn't installed. Inside {@code and} it's only decoded once the entry is active, which means its mods are there.
 *
 * <p>Only server entries own data: the server decides what's in a datapack, and the client follows. A client entry here
 * is a mistake in our own files, reported once and read as off.
 */
public record EntryCondition(String entry, List<Dynamic<?>> and) implements ICondition {
    public static final MapCodec<EntryCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("entry").forGetter(EntryCondition::entry),
        Codec.PASSTHROUGH.listOf().optionalFieldOf("and", List.of()).forGetter(EntryCondition::and)
    ).apply(instance, EntryCondition::new));

    private static final Set<String> REPORTED = new HashSet<>();

    @Override
    public boolean test(IContext context) {
        Optional<Entry> found = Entries.REGISTRY.get(entry);
        if (found.isEmpty())
            return report("names an entry that doesn't exist");
        if (found.get().side() != Side.SERVER)
            return report("names a client entry; only server entries can gate data");
        if (!found.get().active())
            return false;
        for (Dynamic<?> condition : and) {
            ICondition inner = ICondition.CODEC.parse(condition)
                .getOrThrow(problem -> new IllegalStateException("lifeguard:entry '" + entry + "': " + problem));
            if (!inner.test(context))
                return false;
        }
        return true;
    }

    private boolean report(String problem) {
        synchronized (REPORTED) {
            if (REPORTED.add(entry))
                Lifeguard.LOGGER.error("Lifeguard data condition lifeguard:entry '{}' {}; that data stays off", entry, problem);
        }
        return false;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
