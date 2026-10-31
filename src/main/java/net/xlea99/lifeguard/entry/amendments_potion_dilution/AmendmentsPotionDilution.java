package net.xlea99.lifeguard.entry.amendments_potion_dilution;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class AmendmentsPotionDilution {
    /** Potions mix on the server. */
    public static final Entry ENTRY = Entries.fix("amendments_potion_dilution", Taste.LIGHT).home("amendments")
        .requiresMods("amendments")
        .side(Side.SERVER).restart(Restart.GAME);

    /** A leftover share of Instant Health becomes as much Regeneration, of Harming as much Poison, instead of fizzling. */
    public static final Entry LEFTOVERS = Entries.tweak("amendments_potion_dilution_leftovers", Taste.STRONG)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .side(Side.SERVER).restart(Restart.GAME);

    private AmendmentsPotionDilution() {
    }
}
