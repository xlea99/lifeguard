package net.xlea99.lifeguard.entry.cc_extended_plated_bricks;

import net.xlea99.lifeguard.block.PlatedBricks;
import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link CcExtendedPlatedBricks}. */
public final class CcExtendedPlatedBricksSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // Each metal registers whenever its entry is installed, never by the switch: a world may hold them.
        wiring.always(PlatedBricks::register);
    }
}
