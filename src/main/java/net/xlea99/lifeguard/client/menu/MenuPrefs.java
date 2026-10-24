package net.xlea99.lifeguard.client.menu;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.neoforged.fml.loading.FMLPaths;
import net.xlea99.lifeguard.Lifeguard;

/**
 * How the menu itself behaves for this player (only reduced motion, so far). Not an entry and not a game setting, so it
 * stays out of the entry config files: its own small file, {@code config/lifeguard-menu.properties}.
 */
final class MenuPrefs {
    private static final String FILE = "lifeguard-menu.properties";
    private static final String REDUCED_MOTION = "reduced_motion";

    private static Boolean reducedMotion;

    private MenuPrefs() {
    }

    static boolean reducedMotion() {
        if (reducedMotion == null)
            reducedMotion = Boolean.parseBoolean(load().getProperty(REDUCED_MOTION, "false"));
        return reducedMotion;
    }

    static void setReducedMotion(boolean value) {
        reducedMotion = value;
        Properties properties = load();
        properties.setProperty(REDUCED_MOTION, Boolean.toString(value));
        try (Writer writer = Files.newBufferedWriter(path(), StandardCharsets.UTF_8)) {
            properties.store(writer, "Lifeguard menu preferences");
        } catch (IOException e) {
            Lifeguard.LOGGER.warn("Lifeguard couldn't save {}", FILE, e);
        }
    }

    private static Properties load() {
        Properties properties = new Properties();
        Path path = path();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException e) {
                Lifeguard.LOGGER.warn("Lifeguard couldn't read {}; using defaults", FILE, e);
            }
        }
        return properties;
    }

    private static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE);
    }
}
