package net.xlea99.lifeguard.entry.cc_necromium_armor_slowness;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcNecromiumArmorSlowness {
    /** Damage is dealt on the server. */
    public static final Entry ENTRY = Entries.fix("cc_necromium_armor_slowness", Taste.NONE)
        .requiresMods("caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME);

    private CcNecromiumArmorSlowness() {
    }
}
