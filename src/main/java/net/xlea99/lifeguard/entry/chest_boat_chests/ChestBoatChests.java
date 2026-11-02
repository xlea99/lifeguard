package net.xlea99.lifeguard.entry.chest_boat_chests;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ChestBoatChests {
    /**
     * A chest boat remembers the wooden chest it was crafted with (Quark's, Woodworks', Every Compat's) and wears it:
     * one item per boat as ever, the chest in its data ({@link BoatChest}), that chest's own texture shrunk onto the
     * boat's on the player's machine ({@code ChestBoatTextures}). Runtime-gated: crafting, syncing and drawing
     * ask the switch the server synced; the hand-off between item and boat always runs, so switching it off loses no
     * boat's chest. Vanilla's renderer covers vanilla, Blueprint and Vanilla Backport boats; other mods' own renderers
     * are children.
     */
    public static final Entry ENTRY = Entries.compat("chest_boat_chests", Taste.STRONG).home(Entry.MINECRAFT)
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Regions Unexplored draws its boats itself, picking the texture from its own map. */
    public static final Entry REGIONS_UNEXPLORED = Entries.compat("chest_boat_chests_regions_unexplored", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("regions_unexplored")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Quark draws its own woods' boats itself (blossom, azalea, ancient), with vanilla's chest boat model. */
    public static final Entry QUARK = Entries.compat("chest_boat_chests_quark", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("quark")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Alex's Caves draws its chest boats' chest as its own model from its own texture (ChestDrawing.ALEXS_CAVES). */
    public static final Entry ALEXS_CAVES = Entries.compat("chest_boat_chests_alexs_caves", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("alexscaves")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Spawn draws its boats itself, picking the texture from its own map, like Regions Unexplored. */
    public static final Entry SPAWN = Entries.compat("chest_boat_chests_spawn", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("spawn")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private ChestBoatChests() {
    }
}
