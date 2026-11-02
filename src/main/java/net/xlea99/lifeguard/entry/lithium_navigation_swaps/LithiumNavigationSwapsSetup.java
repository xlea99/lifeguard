package net.xlea99.lifeguard.entry.lithium_navigation_swaps;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link LithiumNavigationSwaps}. */
public final class LithiumNavigationSwapsSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(LithiumNavigationSwaps.ENTRY, LithiumCheck::run);
    }
}
