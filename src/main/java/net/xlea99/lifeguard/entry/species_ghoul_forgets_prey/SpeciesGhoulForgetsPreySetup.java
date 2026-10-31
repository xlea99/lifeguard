package net.xlea99.lifeguard.entry.species_ghoul_forgets_prey;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link SpeciesGhoulForgetsPrey}. */
public final class SpeciesGhoulForgetsPreySetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(SpeciesGhoulForgetsPrey.ENTRY, GhoulPrey::register);
    }
}
