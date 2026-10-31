package net.xlea99.lifeguard.entry.dye_depot_rock_candy;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link DyeDepotRockCandy}. */
public final class DyeDepotRockCandySetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // Registered whenever both mods are there, never by the switch: a world may hold the candies.
        wiring.whenInstalled(DyeDepotRockCandy.ENTRY, RockCandies::register);
        wiring.whenActive(DyeDepotRockCandy.ENTRY, RockCandies::registerTab);
    }
}
