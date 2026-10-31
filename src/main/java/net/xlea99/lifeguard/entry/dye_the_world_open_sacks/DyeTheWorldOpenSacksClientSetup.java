package net.xlea99.lifeguard.entry.dye_the_world_open_sacks;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Client setup for {@link DyeTheWorldOpenSacks}. */
public final class DyeTheWorldOpenSacksClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(DyeTheWorldOpenSacks.ENTRY, OpenSacks::register);
    }
}
