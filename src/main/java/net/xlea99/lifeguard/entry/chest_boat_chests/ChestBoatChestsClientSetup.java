package net.xlea99.lifeguard.entry.chest_boat_chests;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;
import net.xlea99.lifeguard.client.ChestIcons;

/** Client setup for {@link ChestBoatChests}. */
public final class ChestBoatChestsClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // The server owns this one: drawing and tooltips check the synced switch.
        wiring.whenInstalled(ChestBoatChests.ENTRY, ClientBoatChests::register);
        wiring.always(ChestIcons::register);
    }
}
