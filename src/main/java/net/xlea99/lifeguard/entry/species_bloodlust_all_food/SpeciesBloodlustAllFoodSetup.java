package net.xlea99.lifeguard.entry.species_bloodlust_all_food;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link SpeciesBloodlustAllFood}. */
public final class SpeciesBloodlustAllFoodSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // Runtime-gated: registered whenever Species is there; the listeners ask the switch (both sides handle these).
        wiring.whenInstalled(SpeciesBloodlustAllFood.ENTRY, BloodlustFood::register);
    }
}
