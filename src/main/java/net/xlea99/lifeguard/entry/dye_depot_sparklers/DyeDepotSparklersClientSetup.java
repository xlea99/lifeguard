package net.xlea99.lifeguard.entry.dye_depot_sparklers;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Client setup for {@link DyeDepotSparklers}. */
public final class DyeDepotSparklersClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenInstalled(DyeDepotSparklers.ENTRY, SparklersClient::register);
    }
}
