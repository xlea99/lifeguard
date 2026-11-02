package net.xlea99.lifeguard.entry.horseman_quark_horses;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class HorsemanQuarkHorses {
    /**
     * None: settles Horseman's two clashes with Quark, the horse mod winning where both set the same thing.
     *
     * <p>Swimming: Quark's Horses Swim lifts every ridden horse-like animal in deep water, every tick, with no input;
     * Horseman's horse_swim_when_ridden lifts it only while its rider holds jump, and never the ones in its
     * {@code #horseman:cannot_swim} tag (camels, skeleton and zombie horses). Both on, the lifts added up: horses floated
     * without input and the "can't swim" ones swam anyway. With Horseman's swimming on, Quark's leaves horses alone; with
     * it off, Quark's still works.
     *
     * <p>Shearing: Horseman's shears_remove_chest drops a plain chest even when the animal wore one of Quark's Variant
     * Chests (Quark keeps it in the animal's data and only gives it back on death), and left that data behind. Now the
     * shears drop the chest it wore.
     *
     * <p>Runtime-gated: the swimming half runs on the client too (a ridden horse moves on its rider's client).
     * (Horseman 1.5.13, Quark 4.1-488, read 2026-10-31.)
     */
    public static final Entry ENTRY = Entries.compat("horseman_quark_horses", Taste.NONE).home("horseman")
        .requiresMods("horseman", "quark")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private HorsemanQuarkHorses() {
    }
}
