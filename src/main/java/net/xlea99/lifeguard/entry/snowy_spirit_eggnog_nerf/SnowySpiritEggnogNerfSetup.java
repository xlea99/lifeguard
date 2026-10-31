package net.xlea99.lifeguard.entry.snowy_spirit_eggnog_nerf;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link SnowySpiritEggnogNerf}. */
public final class SnowySpiritEggnogNerfSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(SnowySpiritEggnogNerf.ENTRY, EggnogNerf::register);
    }
}
