package net.xlea99.lifeguard.entry.species_dyeable_chaindeliers;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SpeciesDyeableChaindeliers {
    /**
     * Runtime-gated: the color lives on the block entity and item whatever the switch (so off and on again loses
     * nothing), and both sides draw or handle it, each asking the switch the server synced. The resource pack holds the
     * atlas source that makes the glow masks. See Chaindeliers.
     */
    public static final Entry ENTRY = Entries.tweak("species_dyeable_chaindeliers", Taste.LIGHT).home("species")
        .requiresMods("species")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated()
        .resourcePack();

    private SpeciesDyeableChaindeliers() {
    }
}
