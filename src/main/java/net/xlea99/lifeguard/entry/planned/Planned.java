package net.xlea99.lifeguard.entry.planned;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.SubValue;
import net.xlea99.lifeguard.core.Taste;

/**
 * lnod_glue entries declared ahead of their port: full metadata, no code. They give the menu every shape it has to
 * handle (a three-level tree, a client child under a server parent, a cross-branch requirement, every taste, all three
 * kinds of sub-value, installed and missing mods) and they are the porting backlog.
 *
 * <p>Planned entries exist in development only; a release build drops them. Class and taste here are placeholders until
 * the taxonomy pass. When one is ported it moves to its own package under {@code entry/} and loses {@code planned()}.
 */
public final class Planned {
    private Planned() {
    }

    // --- features ------------------------------------------------------------------------------------------------------

    public static final Entry JUKEBOX = server(Entries.feature("jukebox", Taste.LIGHT));
    public static final Entry JUKEBOX_PLAYLISTS = server(Entries.feature("playlists", Taste.LIGHT).parent(JUKEBOX));
    public static final Entry JUKEBOX_RADIO = server(Entries.feature("radio", Taste.STRONG).parent(JUKEBOX_PLAYLISTS));
    public static final Entry JUKEBOX_PROGRESS_SIGNAL = server(Entries.feature("progress_signal", Taste.LIGHT).parent(JUKEBOX));
    public static final Entry JUKEBOX_SYNCED_PLAYBACK = server(Entries.feature("synced_playback", Taste.LIGHT).parent(JUKEBOX));
    public static final SubValue.Int MAX_SPEAKERS = SubValue.ofInt("max_speakers", 2, 0, 4);
    public static final Entry JUKEBOX_SPEAKERS = server(Entries.compat("speakers", Taste.LIGHT).parent(JUKEBOX)
        .requiresMods("supplementaries").values(MAX_SPEAKERS));
    public static final Entry JUKEBOX_RETEXTURE = client(Entries.tweak("retexture", Taste.STRONG).parent(JUKEBOX));

    public static final SubValue.Int TWEETER_RANGE = SubValue.ofInt("range", 128, 1, 256);
    public static final Entry TWEETER = server(Entries.feature("tweeter", Taste.STRONG).values(TWEETER_RANGE));
    public static final Entry RUNE_GLINTS = client(Entries.feature("rune_glints", Taste.STRONG).requiresMods("quark"));
    public static final Entry BOIDSIFY = server(Entries.feature("boidsify", Taste.STRONG));

    // --- tweaks --------------------------------------------------------------------------------------------------------

    public static final SubValue.Int FLICKER_MIN_SECONDS = SubValue.ofInt("min_seconds", 10, 1, 600);
    public static final SubValue.Int FLICKER_MAX_SECONDS = SubValue.ofInt("max_seconds", 50, 1, 600);
    public static final SubValue.Int FLICKER_MAX_FLICKERS = SubValue.ofInt("max_flickers", 3, 1, 10);
    public static final Entry BULB_FLICKER = client(Entries.tweak("bulb_flicker", Taste.LIGHT).requiresMods("etcetera")
        .values(FLICKER_MIN_SECONDS, FLICKER_MAX_SECONDS, FLICKER_MAX_FLICKERS));
    public static final SubValue.Decimal MUSIC_FADE_SECONDS = SubValue.ofDecimal("fade_seconds", 1.5, 0, 10);
    public static final SubValue.Bool MUSIC_RESUMES = SubValue.ofBool("resume_on_unpause", true);
    public static final Entry PAUSE_PAUSES_MUSIC = client(Entries.tweak("pause_pauses_music", Taste.LIGHT)
        .values(MUSIC_FADE_SECONDS, MUSIC_RESUMES));
    public static final Entry PARANOID_OCTOPUSES = server(Entries.tweak("paranoid_octopuses", Taste.STRONG).requiresMods("spawn"));
    public static final Entry NERFED_EGGNOG = server(Entries.tweak("nerfed_eggnog", Taste.DEEP_END).requiresMods("snowyspirit"));
    public static final Entry BRIGHTER_GINGER_COLORS = client(Entries.tweak("brighter_ginger_colors", Taste.DEEP_END)
        .requiresMods("snowyspirit"));
    public static final Entry LENIENT_TAG_LOADING = server(Entries.tweak("lenient_tag_loading", Taste.DANGEROUS));

    // --- compat --------------------------------------------------------------------------------------------------------

    public static final Entry OCTOPUS_NETHER_CHESTS = server(Entries.compat("octopus_nether_chests", Taste.NONE)
        .requiresMods("spawn").requiresEntries(PARANOID_OCTOPUSES));
    public static final Entry DYE_DEPOT_SPARKLERS = server(Entries.compat("dye_depot_sparklers", Taste.NONE)
        .requiresMods("dye_depot", "caverns_and_chasms"));
    public static final Entry PLATED_BRICKS = server(Entries.compat("plated_bricks", Taste.LIGHT).requiresMods("caverns_and_chasms"));
    public static final Entry DYE_DEPOT_RUNES = client(Entries.compat("dye_depot_runes", Taste.NONE)
        .requiresMods("quark", "dye_depot"));
    public static final Entry EASY_ANVILS_QUARK_ANVILS = server(Entries.compat("easy_anvils_quark_anvils", Taste.NONE)
        .requiresMods("easyanvils", "quark"));
    public static final Entry CANDLE_HOLDER_SOUL_FLAMES = client(Entries.compat("candle_holder_soul_flames", Taste.NONE)
        .requiresMods("supplementaries", "quark"));

    // --- fixes ---------------------------------------------------------------------------------------------------------

    public static final Entry BULB_RELOAD_DARK = server(Entries.fix("bulb_reload_dark", Taste.NONE).requiresMods("etcetera"));
    public static final SubValue.Int DUCT_MAX_SPAN = SubValue.ofInt("max_span", 8, 1, 64);
    public static final Entry STORAGE_DUCT_SPAN_LIMIT = server(Entries.fix("storage_duct_span_limit", Taste.NONE)
        .requiresMods("caverns_and_chasms").values(DUCT_MAX_SPAN));
    public static final Entry GLARE_GRUMPINESS = server(Entries.fix("glare_grumpiness", Taste.LIGHT).requiresMods("caverns_and_chasms"));
    public static final Entry MAP_ATLASES_FIXES = server(Entries.fix("map_atlases_fixes", Taste.NONE).requiresMods("map_atlases"));
    public static final Entry QUARK_DONKEY_CHESTS = server(Entries.fix("quark_donkey_chests", Taste.NONE).requiresMods("quark"));
    public static final Entry WARDEN_ROAR_LOOP = server(Entries.fix("warden_roar_loop", Taste.NONE));
    public static final Entry BUCKET_MOB_FLICKER = client(Entries.fix("bucket_mob_flicker", Taste.NONE));

    private static Entry server(Entry entry) {
        return entry.side(Side.SERVER).restart(Restart.GAME).planned();
    }

    private static Entry client(Entry entry) {
        return entry.side(Side.CLIENT).restart(Restart.GAME).planned();
    }
}
