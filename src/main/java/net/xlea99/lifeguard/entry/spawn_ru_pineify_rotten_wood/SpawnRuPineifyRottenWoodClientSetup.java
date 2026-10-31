package net.xlea99.lifeguard.entry.spawn_ru_pineify_rotten_wood;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Client setup for {@link SpawnRuPineifyRottenWood}. */
public final class SpawnRuPineifyRottenWoodClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(SpawnRuPineifyRottenWood.ENTRY, PineRotPack::register);
    }
}
