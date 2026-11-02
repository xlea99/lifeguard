package net.xlea99.lifeguard.entry.easy_anvils_quark_anvils;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link EasyAnvilsQuarkAnvils}. */
public final class EasyAnvilsQuarkAnvilsSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(EasyAnvilsQuarkAnvils.ENTRY, AnvilBreaks::register);
    }
}
