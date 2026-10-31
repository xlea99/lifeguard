package net.xlea99.lifeguard.entry.whiter_labels;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Client setup for {@link WhiterLabels}. */
public final class WhiterLabelsClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(WhiterLabels.ENTRY, WhiterPaper::register);
    }
}
