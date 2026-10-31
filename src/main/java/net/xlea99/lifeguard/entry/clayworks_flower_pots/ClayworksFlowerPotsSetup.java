package net.xlea99.lifeguard.entry.clayworks_flower_pots;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link ClayworksFlowerPots}. */
public final class ClayworksFlowerPotsSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // Registered whenever Clayworks is there, never by the switch: placed pots keep their looks while it's off.
        wiring.whenInstalled(ClayworksFlowerPots.ENTRY, FlowerPots::register);
    }
}
