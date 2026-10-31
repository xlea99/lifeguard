package net.xlea99.lifeguard.entry.dye_the_world_glow_lights;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class DyeTheWorldGlowLights {
    /** Only models and textures. */
    public static final Entry ENTRY = Entries.compat("dye_the_world_glow_lights", Taste.NONE).home("snowyspirit")
        .requiresMods("snowyspirit", "dye_the_world")
        .side(Side.CLIENT).restart(Restart.GAME);

    private DyeTheWorldGlowLights() {
    }
}
