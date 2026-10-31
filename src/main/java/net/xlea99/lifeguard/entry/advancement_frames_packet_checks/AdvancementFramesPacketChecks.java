package net.xlea99.lifeguard.entry.advancement_frames_packet_checks;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class AdvancementFramesPacketChecks {
    /** The server checks what clients ask for. */
    public static final Entry ENTRY = Entries.fix("advancement_frames_packet_checks", Taste.NONE)
        .home("advancementframes")
        .requiresMods("advancementframes")
        .side(Side.SERVER).restart(Restart.GAME);

    private AdvancementFramesPacketChecks() {
    }
}
