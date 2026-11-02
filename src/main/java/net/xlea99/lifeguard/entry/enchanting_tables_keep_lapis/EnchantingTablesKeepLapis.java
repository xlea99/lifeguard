package net.xlea99.lifeguard.entry.enchanting_tables_keep_lapis;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class EnchantingTablesKeepLapis {
    /**
     * Lapis left in an enchanting table stays in that table ({@link TableLapis}): one stack per table, saved with it,
     * that every menu open on the table shows. A change in any viewer's lapis slot is saved to the table and copied to
     * every other viewer at once, so two players at one table can never each take the same lapis. Breaking the table
     * drops it. Runtime-gated: keeping lapis asks the switch; the stored stack is always saved and always dropped when
     * the table breaks, so switching it off loses no lapis (off, the slot starts empty and closing hands lapis back, as
     * in vanilla; what a table already holds waits in it).
     */
    public static final Entry ENTRY = Entries.tweak("enchanting_tables_keep_lapis", Taste.LIGHT).home(Entry.MINECRAFT)
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private EnchantingTablesKeepLapis() {
    }
}
