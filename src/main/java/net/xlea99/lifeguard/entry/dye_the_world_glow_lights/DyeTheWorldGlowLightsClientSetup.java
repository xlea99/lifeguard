package net.xlea99.lifeguard.entry.dye_the_world_glow_lights;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Client setup for {@link DyeTheWorldGlowLights}. */
public final class DyeTheWorldGlowLightsClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(DyeTheWorldGlowLights.ENTRY, GlowLightsPack::register);
    }
}
