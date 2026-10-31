package net.xlea99.lifeguard.entry.cc_storage_duct_span_limit;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;
import net.xlea99.lifeguard.duct.StorageDucts;

/** Setup for {@link CcStorageDuctSpanLimit}. */
public final class CcStorageDuctSpanLimitSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(CcStorageDuctSpanLimit.ENTRY, StorageDucts::registerPlaceWarning);
    }
}
