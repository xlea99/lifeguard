package net.xlea99.lifeguard.client.jade;

import net.xlea99.lifeguard.entry.amendments_hanging_pot_names.AmendmentsHangingPotNames;
import net.xlea99.lifeguard.entry.amendments_hanging_pot_names.HangingPotName;
import net.xlea99.lifeguard.entry.moonlight_jade_contents.MoonlightJadeContents;
import net.xlea99.lifeguard.entry.moonlight_jade_contents.SoftFluidContents;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Lifeguard's one Jade plugin, found by Jade only when Jade is installed. Each entry's part registers only while that
 * entry is active, which also means its other mods are there.
 */
@WailaPlugin
public final class LifeguardJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        if (MoonlightJadeContents.ENTRY.active())
            SoftFluidContents.register(registration);
        if (AmendmentsHangingPotNames.ENTRY.active())
            HangingPotName.register(registration);
    }
}
