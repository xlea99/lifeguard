package net.xlea99.lifeguard.entry.ac_thornwood_very_slightly_lighter_planks;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class AcThornwoodVerySlightlyLighterPlanks {
    /**
     * Only textures: thornwood's wood (planks, stripped log, the log end's inside, door, trapdoor, signs, boats, the
     * Forsaken Idol) a hair lighter, OKLab lightness +.02, its color untouched, so it sits a step apart from Regions
     * Unexplored's blackwood. Bark, branches, sapling, sign posts and the idol's eye stay. Made on the player's machine
     * from their own Alex's Caves ({@code client/SpecRecolors}): none of Alex's Caves' art is redistributed.
     */
    public static final Entry ENTRY = Entries.tweak("ac_thornwood_very_slightly_lighter_planks", Taste.DEEP_END)
        .home("alexscaves")
        .requiresMods("alexscaves")
        .side(Side.CLIENT).restart(Restart.GAME);

    private AcThornwoodVerySlightlyLighterPlanks() {
    }
}
