package net.xlea99.lifeguard.entry.amendments_cake_slicing;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link AmendmentsCakeSlicing}. */
public final class AmendmentsCakeSlicingSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(AmendmentsCakeSlicing.ENTRY, CakeSlicing::register);
    }
}
