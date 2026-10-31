package net.xlea99.lifeguard.entry.ru_broken_sounds;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class RuBrokenSounds {
    /**
     * Regions Unexplored 0.6.3 ships six broken sound files (Ogg pages cut short or corrupt, so nothing plays): one in
     * four redwood breaks and placements is silent, and so are some redwood hanging sign and door, baobab fence gate and
     * leaf litter sounds. Our pack re-lists each of those sound events without its broken file ({@code "replace": true}
     * in RU's {@code sounds.json}). An entry rather than loose data only so that packs without Regions Unexplored don't
     * log a missing-file warning per sound; off is RU's sounds as shipped.
     */
    public static final Entry ENTRY = Entries.fix("ru_broken_sounds", Taste.NONE).home("regions_unexplored")
        .requiresMods("regions_unexplored")
        .side(Side.CLIENT).restart(Restart.GAME)
        .resourcePack();

    private RuBrokenSounds() {
    }
}
