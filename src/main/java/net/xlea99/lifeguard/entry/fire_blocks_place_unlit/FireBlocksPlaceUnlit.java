package net.xlea99.lifeguard.entry.fire_blocks_place_unlit;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class FireBlocksPlaceUnlit {
    /**
     * Runtime-gated: a block's placement state is worked out on both sides (the client predicts it), so both ask the
     * switch the server synced. Campfires as Forgery's {@code campfires_place_unlit} had them (every CampfireBlock); the
     * children bring other mods' fire blocks along.
     */
    public static final Entry ENTRY = Entries.feature("fire_blocks_place_unlit", Taste.STRONG).home(Entry.LIFEGUARD)
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Supplementaries' fire pit. */
    public static final Entry SUPPLEMENTARIES = Entries.compat("fire_blocks_place_unlit_supplementaries", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Caverns & Chasms' braziers (plain, soul, ender, cupric). */
    public static final Entry CAVERNS_AND_CHASMS = Entries.compat("fire_blocks_place_unlit_caverns_and_chasms", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private FireBlocksPlaceUnlit() {
    }
}
