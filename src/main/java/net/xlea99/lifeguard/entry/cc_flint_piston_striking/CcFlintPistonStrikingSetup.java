package net.xlea99.lifeguard.entry.cc_flint_piston_striking;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link CcFlintPistonStriking}. */
public final class CcFlintPistonStrikingSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(CcFlintPistonStriking.ENTRY, FlintStriking::register);
    }
}
