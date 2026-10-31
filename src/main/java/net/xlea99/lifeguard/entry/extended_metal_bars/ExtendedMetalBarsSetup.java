package net.xlea99.lifeguard.entry.extended_metal_bars;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link ExtendedMetalBars}. */
public final class ExtendedMetalBarsSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // Each metal registers whenever its entry (or its mod) is there, never by the switch: a world may hold them.
        wiring.always(Bars::register);
    }
}
