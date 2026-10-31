package net.xlea99.lifeguard.entry.twigs_pebble_dispensing;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link TwigsPebbleDispensing}. */
public final class TwigsPebbleDispensingSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(TwigsPebbleDispensing.ENTRY, PebbleDispensers::register);
    }
}
