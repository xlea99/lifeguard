package net.xlea99.lifeguard.entry.ac_thornwood_very_slightly_lighter_planks;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;
import net.xlea99.lifeguard.client.SpecRecolors;

/** Client setup for {@link AcThornwoodVerySlightlyLighterPlanks}. */
public final class AcThornwoodVerySlightlyLighterPlanksClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(AcThornwoodVerySlightlyLighterPlanks.ENTRY, () -> SpecRecolors.register(
            AcThornwoodVerySlightlyLighterPlanks.ENTRY.id(), SpecRecolors.spec("ac_thornwood"), "alexscaves"));
    }
}
