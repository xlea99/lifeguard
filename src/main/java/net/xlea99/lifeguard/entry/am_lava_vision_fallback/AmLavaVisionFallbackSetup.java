package net.xlea99.lifeguard.entry.am_lava_vision_fallback;

import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link AmLavaVisionFallback}. */
public final class AmLavaVisionFallbackSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(AmLavaVisionFallback.ENTRY, LavaVision::register);
    }
}
