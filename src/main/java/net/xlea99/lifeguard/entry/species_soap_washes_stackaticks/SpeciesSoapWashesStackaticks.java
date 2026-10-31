package net.xlea99.lifeguard.entry.species_soap_washes_stackaticks;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SpeciesSoapWashesStackaticks {
    /**
     * Runtime-gated: a right-click is handled on both sides (the client mustn't make the Stackatick sit), so the listener
     * is registered whenever both mods are there and asks the switch. See StackatickSoap.
     */
    public static final Entry ENTRY = Entries.compat("species_soap_washes_stackaticks", Taste.NONE)
        .requiresMods("species", "supplementaries")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private SpeciesSoapWashesStackaticks() {
    }
}
