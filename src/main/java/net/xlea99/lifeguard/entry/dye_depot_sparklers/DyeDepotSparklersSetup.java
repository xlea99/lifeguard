package net.xlea99.lifeguard.entry.dye_depot_sparklers;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link DyeDepotSparklers}. */
public final class DyeDepotSparklersSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // Runtime-gated: registered whenever both mods are there (a world may hold the sparklers); the listeners check the
        // switch.
        wiring.whenInstalled(DyeDepotSparklers.ENTRY, Sparklers::register);
    }
}
