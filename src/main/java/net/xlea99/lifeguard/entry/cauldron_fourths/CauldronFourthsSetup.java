package net.xlea99.lifeguard.entry.cauldron_fourths;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link CauldronFourths}. */
public final class CauldronFourthsSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(CauldronFourths.ENTRY, FourLevelInteractions::register);
    }
}
