package net.xlea99.lifeguard.entry.quark_elytra_duplication_crash;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class QuarkElytraDuplicationCrash {
    /**
     * Runtime-gated: crafting runs on both sides (the client predicts the result slot), so the client needs the mixin
     * too, and it has to follow the server's setting rather than the player's local file.
     */
    public static final Entry ENTRY = Entries.fix("quark_elytra_duplication_crash", Taste.NONE).home("quark")
        .requiresMods("quark")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private QuarkElytraDuplicationCrash() {
    }
}
