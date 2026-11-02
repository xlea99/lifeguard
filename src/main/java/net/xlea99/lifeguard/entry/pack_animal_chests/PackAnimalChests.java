package net.xlea99.lifeguard.entry.pack_animal_chests;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class PackAnimalChests {
    /**
     * Donkeys, mules, llamas and trader llamas wear the chest they were given, where a mod lets them take any wooden
     * chest and remembers which (Woodworks, synced to clients by Blueprint; Quark's Variant Chests, kept on the server
     * only, sent by us: {@link PackAnimalChest}). Their saddlebags are a hand-drawn icon of a chest (8x8x3, 6x6 inside the
     * frame), too small to draw the chest's own texture onto, so each is recolored from it by role on the player's
     * machine ({@code SaddlebagDrawing}). Runtime-gated: the syncing and drawing ask the switch the server synced.
     */
    public static final Entry ENTRY = Entries.compat("pack_animal_chests", Taste.STRONG).home(Entry.MINECRAFT)
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /**
     * Alex's Mobs' elephants take any wooden chest too, and forgot which (a plain chest came back on shearing or death):
     * {@link ElephantChests}.
     */
    public static final Entry ALEXS_MOBS = Entries.compat("pack_animal_chests_alexs_mobs", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("alexsmobs")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private PackAnimalChests() {
    }
}
