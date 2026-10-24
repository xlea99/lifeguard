package net.xlea99.lifeguard.boot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Registry;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.config.EntryConfig;
import net.xlea99.lifeguard.core.config.Texts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lifeguard's startup: declarations, validation, config files. Runs once, as early as anything of ours runs (the mixin
 * plugin calls it before any mixin is decided), so everything after it can trust that entries resolve.
 *
 * <p>This runs before Minecraft's classes are safe to load. It may use the mod loader's early classes, and nothing from
 * Minecraft itself; the workspace tests check that.
 */
public final class Boot {
    private static final Logger LOGGER = LoggerFactory.getLogger("Lifeguard");
    private static final String LANG = "/assets/lifeguard/lang/en_us.json";

    private static volatile boolean started;
    private static EntryConfig clientConfig;
    private static EntryConfig serverConfig;

    private Boot() {
    }

    public static synchronized void start() {
        if (started)
            return;
        Registry registry = Entries.REGISTRY;
        Catalog.load();

        Map<String, String> lang = readLang();
        LoadingModList mods = LoadingModList.get();
        registry.setModPresence(modId -> mods.getModFileById(modId) != null);
        // Throws, listing every problem, if a declaration is wrong: a crash here is a bug in Lifeguard, found early.
        registry.freeze(lang.keySet(), !FMLLoader.isProduction());

        Texts texts = new Texts(lang, Boot::modName);
        Path dir = FMLPaths.CONFIGDIR.get();
        serverConfig = open(new EntryConfig(dir.resolve("lifeguard-server.toml"), Side.SERVER, registry, texts));
        // A dedicated server never reads or writes the client file: those choices belong to each player.
        if (FMLLoader.getDist().isClient())
            clientConfig = open(new EntryConfig(dir.resolve("lifeguard-client.toml"), Side.CLIENT, registry, texts));

        started = true;
        LOGGER.info("Lifeguard: {}", summary(registry));
    }

    /** The client's own file. Empty on a dedicated server. */
    public static Optional<EntryConfig> clientConfig() {
        return Optional.ofNullable(clientConfig);
    }

    public static EntryConfig serverConfig() {
        return serverConfig;
    }

    public static boolean dedicatedServer() {
        return !FMLLoader.getDist().isClient();
    }

    private static Map<String, String> readLang() {
        try (InputStream json = Boot.class.getResourceAsStream(LANG)) {
            if (json == null)
                throw new IllegalStateException("Lifeguard's " + LANG + " is missing from the jar");
            return Texts.readLang(json);
        } catch (IOException e) {
            throw new IllegalStateException("Lifeguard couldn't read " + LANG, e);
        }
    }

    private static EntryConfig open(EntryConfig config) {
        String name = config.file().getFileName().toString();
        try {
            List<String> warnings = config.load();
            for (String warning : warnings)
                LOGGER.warn("Lifeguard {}: {}", name, warning);
            if (config.broken())
                LOGGER.error("Lifeguard {} can't be read and was left untouched; defaults are in use this session", name);
            else if (config.save())
                LOGGER.info("Lifeguard wrote {}", name);
        } catch (IOException e) {
            // Reading or writing the file failed outright (permissions, a locked file): defaults this session.
            LOGGER.error("Lifeguard couldn't read or write {}; defaults are in use this session", name, e);
        }
        return config;
    }

    /** The installed mod's display name, or null when it isn't installed (Texts then falls back to our lang file). */
    private static String modName(String modId) {
        for (ModInfo mod : LoadingModList.get().getMods())
            if (mod.getModId().equals(modId))
                return mod.getDisplayName();
        return null;
    }

    private static String summary(Registry registry) {
        int active = 0, inert = 0, planned = 0;
        for (Entry entry : registry.entries()) {
            if (entry.isPlanned())
                planned++;
            if (!entry.installed())
                inert++;
            else if (entry.active())
                active++;
        }
        StringBuilder out = new StringBuilder();
        int total = registry.entries().size();
        out.append(total).append(total == 1 ? " entry, " : " entries, ").append(active).append(" active, ")
            .append(inert).append(" missing a mod");
        if (planned > 0)
            out.append(", ").append(planned).append(" planned");
        out.append(" · server profile ").append(registry.profile(Side.SERVER).id());
        if (clientConfig != null)
            out.append(" · client profile ").append(registry.profile(Side.CLIENT).id());
        return out.toString();
    }
}
