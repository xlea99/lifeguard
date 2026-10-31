package net.xlea99.lifeguard.entry.snowy_spirit_edible_treats;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Client setup for {@link SnowySpiritEdibleTreats}. */
public final class SnowySpiritEdibleTreatsClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(SnowySpiritEdibleTreats.ENTRY, GumdropCrumbs::register);
    }
}
