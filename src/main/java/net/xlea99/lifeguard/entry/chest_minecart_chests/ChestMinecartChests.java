package net.xlea99.lifeguard.entry.chest_minecart_chests;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ChestMinecartChests {
    /**
     * A chest minecart remembers the wooden chest it was crafted with (vanilla's recipe already takes any, NeoForge's
     * widening, and forgot which) and wears it: one item as ever, the chest in its data ({@link CartChest}), and the
     * cart draws that chest's own block, through the chest's own item renderer, so nothing is drawn by us. Runtime-gated
     * like chest boats: crafting, syncing and drawing ask the switch the server synced; the hand-off between item and
     * cart always runs, so switching it off loses no cart's chest.
     */
    public static final Entry ENTRY = Entries.compat("chest_minecart_chests", Taste.STRONG).home(Entry.MINECRAFT)
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Quark's Minecart Upgrade recipe (five iron around any wooden chest) is shaped: its carts remember the chest too. */
    public static final Entry QUARK = Entries.compat("chest_minecart_chests_quark", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("quark")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private ChestMinecartChests() {
    }
}
