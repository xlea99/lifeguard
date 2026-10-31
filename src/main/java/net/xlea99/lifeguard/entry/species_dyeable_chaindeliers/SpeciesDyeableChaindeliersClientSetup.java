package net.xlea99.lifeguard.entry.species_dyeable_chaindeliers;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Client setup for {@link SpeciesDyeableChaindeliers}. */
public final class SpeciesDyeableChaindeliersClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenInstalled(SpeciesDyeableChaindeliers.ENTRY, ChaindelierClient::register);
    }
}
