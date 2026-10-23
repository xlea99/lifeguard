package net.xlea99.lifeguard.core.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * The words the config file's comments are made of: Lifeguard's own lang text and the names of other mods.
 *
 * <p>Read straight from {@code en_us.json} in the jar rather than through the game's language system, which a dedicated
 * server doesn't load for mods and which isn't running yet when the config is first read.
 */
public final class Texts {
    private final Map<String, String> lang;
    private final Function<String, String> modNames;

    public Texts(Map<String, String> lang, Function<String, String> modNames) {
        this.lang = Map.copyOf(lang);
        this.modNames = Objects.requireNonNull(modNames);
    }

    /** Parses an {@code en_us.json}: one flat object of string keys and string values. */
    public static Map<String, String> readLang(InputStream json) throws IOException {
        try (Reader reader = new InputStreamReader(json, StandardCharsets.UTF_8)) {
            JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
            Map<String, String> lang = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : object.entrySet())
                lang.put(entry.getKey(), entry.getValue().getAsString());
            return lang;
        } catch (RuntimeException e) {
            throw new IOException("en_us.json isn't a flat object of strings: " + e.getMessage(), e);
        }
    }

    public Set<String> keys() {
        return lang.keySet();
    }

    /** The text for a key, or the key itself if it's missing (the freeze refuses missing keys, so this is a fallback). */
    public String lang(String key) {
        return lang.getOrDefault(key, key);
    }

    /** A mod's display name from its id, e.g. {@code supplementaries} to {@code Supplementaries}. */
    public String modName(String modId) {
        String name = modNames.apply(modId);
        return name == null || name.isBlank() ? modId : name;
    }
}
