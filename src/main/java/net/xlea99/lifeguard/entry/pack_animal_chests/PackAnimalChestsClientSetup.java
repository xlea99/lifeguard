package net.xlea99.lifeguard.entry.pack_animal_chests;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Client setup for {@link PackAnimalChests}. */
public final class PackAnimalChestsClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // The server owns this one: drawing checks the synced switch.
        wiring.whenInstalled(PackAnimalChests.ENTRY, ClientPackAnimalChests::register);
    }
}
