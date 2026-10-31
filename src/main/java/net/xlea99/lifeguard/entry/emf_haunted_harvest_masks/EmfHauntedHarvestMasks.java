package net.xlea99.lifeguard.entry.emf_haunted_harvest_masks;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class EmfHauntedHarvestMasks {
    /** Only how the mask is drawn. */
    public static final Entry ENTRY = Entries.compat("emf_haunted_harvest_masks", Taste.NONE).home("hauntedharvest")
        .requiresMods("hauntedharvest", "entity_model_features")
        .side(Side.CLIENT).restart(Restart.GAME);

    private EmfHauntedHarvestMasks() {
    }
}
