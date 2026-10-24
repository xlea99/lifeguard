package net.xlea99.lifeguard.entry.sexy_runic_glints;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

/**
 * A retexture of Quark's rune glints: each color's own texture, blend and shading, set in
 * {@code assets/lifeguard/glint_materials.json}. Load-gated: off, none of it loads and Quark draws its own glints
 * (Dye Depot's colors then get {@code dye_depot_runes}' plain Quark-style ones).
 */
public final class SexyRunicGlints {
    public static final Entry ENTRY = Entries.tweak("sexy_runic_glints", Taste.STRONG)
        .requiresMods("quark")
        .side(Side.CLIENT).restart(Restart.GAME);

    private SexyRunicGlints() {
    }
}
