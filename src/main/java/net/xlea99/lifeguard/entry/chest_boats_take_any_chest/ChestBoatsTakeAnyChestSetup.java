package net.xlea99.lifeguard.entry.chest_boats_take_any_chest;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;
import net.xlea99.lifeguard.data.RecipeEdits;

/** Setup for {@link ChestBoatsTakeAnyChest}. */
public final class ChestBoatsTakeAnyChestSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(ChestBoatsTakeAnyChest.ENTRY,
            () -> RecipeEdits.addRule(ChestBoatsTakeAnyChest.ENTRY, StandardChestBoatRecipes::widen));
    }
}
