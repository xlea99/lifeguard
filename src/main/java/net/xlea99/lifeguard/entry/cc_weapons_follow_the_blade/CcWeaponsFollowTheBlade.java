package net.xlea99.lifeguard.entry.cc_weapons_follow_the_blade;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcWeaponsFollowTheBlade {
    /** Damage is dealt on the server. */
    public static final Entry ENTRY = Entries.fix("cc_weapons_follow_the_blade", Taste.NONE)
        .requiresMods("caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME);

    private CcWeaponsFollowTheBlade() {
    }
}
