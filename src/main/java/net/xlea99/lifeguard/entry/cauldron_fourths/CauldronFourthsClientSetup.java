package net.xlea99.lifeguard.entry.cauldron_fourths;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Client setup for {@link CauldronFourths}. */
public final class CauldronFourthsClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(CauldronFourths.ENTRY, FourthsClient::register);
    }
}
