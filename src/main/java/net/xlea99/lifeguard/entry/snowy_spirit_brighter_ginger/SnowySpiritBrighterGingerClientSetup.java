package net.xlea99.lifeguard.entry.snowy_spirit_brighter_ginger;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;
import net.xlea99.lifeguard.client.SpecRecolors;

/** Client setup for {@link SnowySpiritBrighterGinger}. */
public final class SnowySpiritBrighterGingerClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(SnowySpiritBrighterGinger.ENTRY, () -> SpecRecolors.register(SnowySpiritBrighterGinger.ENTRY.id(),
            SpecRecolors.spec("snowy_spirit_ginger"), "snowyspirit"));
    }
}
