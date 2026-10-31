package net.xlea99.lifeguard.entry.species_stackatick_breeding;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link SpeciesStackatickBreeding}. */
public final class SpeciesStackatickBreedingSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // Runtime-gated: registered whenever Species is there; the listeners ask the switch (both sides handle these).
        wiring.whenInstalled(SpeciesStackatickBreeding.ENTRY, StackatickBreeding::register);
    }
}
