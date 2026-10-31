package net.xlea99.lifeguard.entry.cc_bone_flute_checks;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcBoneFluteChecks {
    /** The server takes the commands. See BoneFluteCommands. */
    public static final Entry ENTRY = Entries.fix("cc_bone_flute_checks", Taste.NONE)
        .requiresMods("caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME);

    private CcBoneFluteChecks() {
    }
}
