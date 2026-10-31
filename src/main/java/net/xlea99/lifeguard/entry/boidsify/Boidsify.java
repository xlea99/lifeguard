package net.xlea99.lifeguard.entry.boidsify;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class Boidsify {
    /**
     * Fish AI is the server's. Ships no fish: which species school, and how, comes from datapacks (Deep End keeps its own
     * in {@code tweaks_deep_end}). Meant to spin out into its own small mod one day. See {@code boids.Boids}.
     */
    public static final Entry ENTRY = Entries.feature("boidsify", Taste.LIGHT).home(Entry.MINECRAFT)
        .side(Side.SERVER).restart(Restart.GAME);

    private Boidsify() {
    }
}
