package net.xlea99.lifeguard.entry.pack_animal_chests;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link PackAnimalChests}. */
public final class PackAnimalChestsSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // Runtime-gated: the payload is registered whenever it could be sent; the listener asks the switch.
        wiring.whenInstalled(PackAnimalChests.ENTRY, bus -> {
            bus.addListener(PackAnimalChestPayload::register);
            PackAnimalChest.register();
        });
    }
}
