package net.xlea99.lifeguard.entry.themed_candle_holder_flames;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ThemedCandleHolderFlames {
    /** Flames are particles: client only. */
    public static final Entry ENTRY = Entries.compat("themed_candle_holder_flames", Taste.NONE)
        .requiresMods("supplementaries", "quark")
        .side(Side.CLIENT).restart(Restart.GAME);

    /** Candles, candle cakes and candle holders on copper burn with Caverns & Chasms' cupric flames. */
    public static final Entry CUPRIC = Entries.compat("cupric", Taste.LIGHT).parent(ENTRY)
        .requiresMods("caverns_and_chasms")
        .side(Side.CLIENT).restart(Restart.GAME);

    private ThemedCandleHolderFlames() {
    }
}
