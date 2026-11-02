package net.xlea99.lifeguard.entry.enchanting_tables_keep_lapis;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link EnchantingTablesKeepLapis}. */
public final class EnchantingTablesKeepLapisSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // Runtime-gated: the table's storage is registered whenever it could hold lapis; the menu hooks ask the switch.
        wiring.whenInstalled(EnchantingTablesKeepLapis.ENTRY, TableLapis::register);
    }
}
