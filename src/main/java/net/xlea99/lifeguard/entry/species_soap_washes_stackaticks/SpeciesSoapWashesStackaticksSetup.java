package net.xlea99.lifeguard.entry.species_soap_washes_stackaticks;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link SpeciesSoapWashesStackaticks}. */
public final class SpeciesSoapWashesStackaticksSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // Runtime-gated: registered whenever both mods are there; the listeners ask the switch (both sides handle these).
        wiring.whenInstalled(SpeciesSoapWashesStackaticks.ENTRY, StackatickSoap::register);
    }
}
