package net.xlea99.lifeguard.entry.scholar_supps_book_piles;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Client setup for {@link ScholarSuppsBookPiles}. */
public final class ScholarSuppsBookPilesClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(ScholarSuppsBookPiles.ENTRY, PileCovers::register);
    }
}
