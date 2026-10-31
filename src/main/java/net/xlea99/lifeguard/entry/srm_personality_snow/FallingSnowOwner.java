package net.xlea99.lifeguard.entry.srm_personality_snow;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Which mod's falling snow runs this session, read from both mods' config files before the game's classes load. A
 * missing file or key is that mod's default: on. Snow! Real Magic is the snow specialist, so Personality's only runs
 * when it's on and SRM's gravity is off.
 */
final class FallingSnowOwner {
    private static final Logger LOGGER = LoggerFactory.getLogger("Lifeguard");
    private static final String PERSONALITY_FILE = "personality-common.toml";
    private static final List<String> PERSONALITY_KEY = List.of("tweaks", "Falling snow layers");
    private static final String SRM_FILE = "snowrealmagic-common.yaml";
    private static final Pattern SRM_GRAVITY = Pattern.compile("^snowGravity:\\s*(true|false)\\s*$", Pattern.MULTILINE);

    private static Boolean personalityYields;

    private FallingSnowOwner() {
    }

    static synchronized boolean personalityYields() {
        if (personalityYields == null) {
            Path dir = FMLPaths.CONFIGDIR.get();
            boolean personality = personalityFallingSnow(dir.resolve(PERSONALITY_FILE));
            boolean srm = srmGravity(dir.resolve(SRM_FILE));
            if (personality && srm)
                LOGGER.warn("Lifeguard srm_personality_snow: Personality's \"Falling snow layers\" and Snow! Real Magic's"
                    + " snowGravity are both on, so Snow! Real Magic's falling snow wins. To use Personality's, set"
                    + " snowGravity: false in {}", SRM_FILE);
            personalityYields = !personality || srm;
        }
        return personalityYields;
    }

    private static boolean personalityFallingSnow(Path file) {
        String text = read(file);
        if (text == null)
            return true;
        try {
            CommentedConfig root = TomlFormat.newConfig(LinkedHashMap::new);
            new TomlParser().parse(new StringReader(text), root, ParsingMode.REPLACE);
            return !(root.get(PERSONALITY_KEY) instanceof Boolean on) || on;
        } catch (RuntimeException e) {
            LOGGER.warn("Lifeguard srm_personality_snow: couldn't read {}; taking Personality's default (on)", file, e);
            return true;
        }
    }

    private static boolean srmGravity(Path file) {
        String text = read(file);
        if (text == null)
            return true;
        Matcher match = SRM_GRAVITY.matcher(text);
        return !match.find() || Boolean.parseBoolean(match.group(1));
    }

    private static String read(Path file) {
        if (!Files.exists(file))
            return null;
        try {
            return Files.readString(file);
        } catch (IOException e) {
            LOGGER.warn("Lifeguard srm_personality_snow: couldn't read {}; taking its mod's default (on)", file, e);
            return null;
        }
    }
}
