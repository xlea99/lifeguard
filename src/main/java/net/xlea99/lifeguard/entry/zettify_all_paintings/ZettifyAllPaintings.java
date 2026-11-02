package net.xlea99.lifeguard.entry.zettify_all_paintings;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ZettifyAllPaintings {
    /**
     * Strong: every registered painting (vanilla's, DarkPaintings', any mod's {@code painting_variant}) becomes a real
     * Zetter painting, framed, signed and copied like a player's own, carrying no pixels anywhere ({@link VirtualPaintings}):
     * each client draws it from its own resource packs, exactly as vanilla paintings work, so no art is shipped or
     * stored and resource packs apply. Its Zetter code holds the original id ({@code lifeguard_painting_minecraft/alban}).
     * Animated paintings and shapes Zetter can't frame are skipped. For now they're in the creative tab (testing).
     * The safety hooks (the server's stub for a virtual code, never sending or requesting pixels for one) stay on whenever
     * Zetter is installed, so a world holding virtual paintings never breaks with the switch off. Runtime-gated:
     * the client registers them when it joins a world, hence a rejoin to switch.
     */
    public static final Entry ENTRY = Entries.feature("zettify_all_paintings", Taste.STRONG).home("zetter")
        .requiresMods("zetter")
        .side(Side.SERVER).restart(Restart.WORLD)
        .runtimeGated();

    /**
     * None: a hung virtual painting gets vanilla's painting lighting (entitySolid + the wall's normal: shaded by the
     * wall's direction, like the wall and Zetter's own frame), as the art was drawn to be seen. Zetter draws every
     * canvas like a map (RenderType.text: light level only, no directional shading), so these looked brighter than the
     * same painting hung by vanilla (user, 2026-11-01). Player paintings keep Zetter's flat look: they were painted on
     * the flat easel screen. Only the hung canvas; easel, paint screen, hand and tooltip are untouched.
     */
    public static final Entry SHADING = Entries.tweak("zettify_all_paintings_shading", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("zetter")
        .sharesMixins("zetter_canvas_shading", "zetter")
        .side(Side.CLIENT).restart(Restart.GAME);

    /**
     * Deep End: every hung Zetter painting, player-made too, gets the same vanilla lighting as {@link #SHADING} (user,
     * 2026-11-01: "i wonder if it wouldn't just look really good"). Trades the easel's exact colors for paintings that
     * sit on the wall like everything else.
     */
    public static final Entry SHADING_ALL = Entries.tweak("zettify_all_paintings_shading_all", Taste.DEEP_END)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("zetter")
        .sharesMixins("zetter_canvas_shading", "zetter")
        .side(Side.CLIENT).restart(Restart.GAME);

    private ZettifyAllPaintings() {
    }
}
