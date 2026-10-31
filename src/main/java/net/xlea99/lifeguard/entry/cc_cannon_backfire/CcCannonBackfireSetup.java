package net.xlea99.lifeguard.entry.cc_cannon_backfire;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link CcCannonBackfire}. */
public final class CcCannonBackfireSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(CcCannonBackfire.ENTRY, CannonBackfire::register);
    }
}
