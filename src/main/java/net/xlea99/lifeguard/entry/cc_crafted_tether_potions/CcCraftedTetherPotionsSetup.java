package net.xlea99.lifeguard.entry.cc_crafted_tether_potions;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link CcCraftedTetherPotions}. */
public final class CcCraftedTetherPotionsSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // Runtime-gated: the recipe type whenever C&C is there; the recipe file and the brewing cut ask the switch.
        wiring.whenInstalled(CcCraftedTetherPotions.ENTRY, CraftedTetherPotions::register);
    }
}
