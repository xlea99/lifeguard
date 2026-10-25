package net.xlea99.lifeguard.entry.supps_continuous_hand_ropes;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsContinuousHandRopes {
    /** The server moves the blocks; the client only predicts whether the click took. */
    public static final Entry ENTRY = Entries.tweak("supps_continuous_hand_ropes", Taste.LIGHT)
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME);

    private SuppsContinuousHandRopes() {
    }
}
