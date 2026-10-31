package net.xlea99.lifeguard.entry.haunted_harvest_torch_lanterns;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class HauntedHarvestTorchLanterns {
    /**
     * The lanterns are registered whenever Haunted Harvest is (never by the switch: placed ones survive it off), so the
     * mixins always apply and the switch only gates lighting them.
     */
    public static final Entry ENTRY = Entries.feature("haunted_harvest_torch_lanterns", Taste.LIGHT)
        .home("hauntedharvest")
        .requiresMods("hauntedharvest")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private HauntedHarvestTorchLanterns() {
    }
}
