package net.xlea99.lifeguard.entry.species_bloodlust_all_food;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SpeciesBloodlustAllFood {
    /**
     * Runtime-gated: eating starts on both sides (the client stops itself too, so it never starts for a moment), so the
     * listener is registered whenever Species is there and both sides ask the switch the server synced. See BloodlustFood.
     */
    public static final Entry ENTRY = Entries.fix("species_bloodlust_all_food", Taste.NONE).home("species")
        .requiresMods("species")
        .usesIds("mob_effect", "species:bloodlust")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private SpeciesBloodlustAllFood() {
    }
}
