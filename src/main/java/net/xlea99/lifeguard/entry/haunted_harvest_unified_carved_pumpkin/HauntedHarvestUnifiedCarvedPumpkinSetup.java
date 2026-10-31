package net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link HauntedHarvestUnifiedCarvedPumpkin}. */
public final class HauntedHarvestUnifiedCarvedPumpkinSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // Runtime-gated: the listeners check the switch, so a server can turn it on or off for its world.
        wiring.whenInstalled(HauntedHarvestUnifiedCarvedPumpkin.ENTRY, UnifiedPumpkin::register);
        wiring.whenInstalled(HauntedHarvestUnifiedCarvedPumpkin.ENTRY, CarvingPools::register);
    }
}
