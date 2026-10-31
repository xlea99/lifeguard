package net.xlea99.lifeguard.entry.clayworks_flower_pots;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Client setup for {@link ClayworksFlowerPots}. */
public final class ClayworksFlowerPotsClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenInstalled(ClayworksFlowerPots.ENTRY, FlowerPotsClient::register);
    }
}
