package net.xlea99.lifeguard.entry.scholar_default_packs;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Client setup for {@link ScholarDefaultPacks}. */
public final class ScholarDefaultPacksClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(ScholarDefaultPacks.ENTRY, UnsavedPacks::register);
    }
}
