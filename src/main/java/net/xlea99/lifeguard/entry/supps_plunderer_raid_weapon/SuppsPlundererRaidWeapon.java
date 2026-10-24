package net.xlea99.lifeguard.entry.supps_plunderer_raid_weapon;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsPlundererRaidWeapon {
    /**
     * Raids run on the server only. Light: the bug is the crossbow; the sword (and its enchantments) in its place is
     * Lifeguard's call.
     */
    public static final Entry ENTRY = Entries.fix("supps_plunderer_raid_weapon", Taste.LIGHT)
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME);

    private SuppsPlundererRaidWeapon() {
    }
}
