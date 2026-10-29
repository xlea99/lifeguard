package net.xlea99.lifeguard.entry.vanilla_backport_resin_trim_icons;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class VanillaBackportResinTrimIcons {
    /**
     * Vanilla Backport's resin joins Blueprint's generated trim icons, the system Abnormals' own materials use. Client
     * only, both halves in one entry: the icon sprites (our pack) and the overrides (Blueprint), so off is exactly the
     * old look, never icons pointing at sprites that aren't there.
     */
    public static final Entry ENTRY = Entries.compat("vanilla_backport_resin_trim_icons", Taste.NONE)
        .requiresMods("vanillabackport", "blueprint")
        .side(Side.CLIENT).restart(Restart.GAME)
        .resourcePack();

    private VanillaBackportResinTrimIcons() {
    }
}
