package net.xlea99.lifeguard.entry.etcetera_shimmering_bismuth_trims;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class EtceteraShimmeringBismuthTrims {
    /** Light: follows Etcetera's own precedent (bismuth shimmers) where its trim couldn't. Only textures. */
    public static final Entry ENTRY = Entries.tweak("etcetera_shimmering_bismuth_trims", Taste.LIGHT).home("etcetera")
        .requiresMods("etcetera")
        .side(Side.CLIENT).restart(Restart.GAME)
        .resourcePack();

    private EtceteraShimmeringBismuthTrims() {
    }
}
