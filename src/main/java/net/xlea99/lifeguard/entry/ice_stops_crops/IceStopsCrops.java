package net.xlea99.lifeguard.entry.ice_stops_crops;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class IceStopsCrops {
    /**
     * A plant whose block two below it is ice (`#minecraft:ice`: ice, packed, blue, frosted, and any mod's ice in the
     * tag) doesn't grow: a crop over its farmland over ice, sugar cane over sand over ice. Everything that asks NeoForge
     * before growing counts (crops, stems, cocoa, cactus, sugar cane, nether wart, berries, bamboo, kelp and vines,
     * chorus). Server only: growth is server-side.
     */
    public static final Entry ENTRY = Entries.tweak("ice_stops_crops", Taste.STRONG).home(Entry.MINECRAFT)
        .side(Side.SERVER).restart(Restart.GAME);

    private IceStopsCrops() {
    }
}
