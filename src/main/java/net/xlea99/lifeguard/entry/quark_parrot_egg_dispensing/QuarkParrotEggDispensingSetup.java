package net.xlea99.lifeguard.entry.quark_parrot_egg_dispensing;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link QuarkParrotEggDispensing}. */
public final class QuarkParrotEggDispensingSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(QuarkParrotEggDispensing.ENTRY, ParrotEggDispensers::register);
    }
}
