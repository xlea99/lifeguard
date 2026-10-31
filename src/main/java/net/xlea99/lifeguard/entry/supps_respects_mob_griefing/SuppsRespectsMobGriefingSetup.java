package net.xlea99.lifeguard.entry.supps_respects_mob_griefing;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link SuppsRespectsMobGriefing}. */
public final class SuppsRespectsMobGriefingSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(SuppsRespectsMobGriefing.ENTRY, MobFiredCannonballs::register);
    }
}
