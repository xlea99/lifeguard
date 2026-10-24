package net.xlea99.lifeguard.entry.sexy_runic_glints;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.Lifeguard;

/** One rune color's glint settings; see {@code glint_materials.json} for what each field does. */
public record GlintMaterial(Blend blend, @Nullable ResourceLocation texture, float colorize, float strength, float selective,
                            @Nullable Ramp ramp, boolean vanilla) {

    /** At most this many colors in a ramp (the shader's fixed number of slots). */
    public static final int MAX_RAMP_STOPS = 8;

    /**
     * Colorize along a gradient instead of one color: the item's brightness picks a color from {@code stops}, dark to
     * light, each sitting at its own brightness. Over the field ({@code field}, where the glint texture is black) and
     * under a ray ({@code ray}, where it's white), the item's black to white spans that stretch of the ramp.
     *
     * @param stops red, green, blue (0-1) and brightness of each color, darkest first
     */
    public record Ramp(float[][] stops, float fieldLow, float fieldHigh, float rayLow, float rayHigh) {

        static Ramp fromJson(JsonObject json) {
            JsonArray colors = json.getAsJsonArray("ramp");
            float[][] stops = new float[Math.min(colors.size(), MAX_RAMP_STOPS)][];
            for (int i = 0; i < stops.length; i++) {
                float[] rgb = rgb(colors.get(i).getAsString());
                stops[i] = new float[] { rgb[0], rgb[1], rgb[2], 0.2126F * rgb[0] + 0.7152F * rgb[1] + 0.0722F * rgb[2] };
            }
            Arrays.sort(stops, (a, b) -> Float.compare(a[3], b[3]));
            float[] field = pair(json, "ramp_field", 0, 1), ray = pair(json, "ramp_ray", field[0], field[1]);
            return new Ramp(stops, field[0], field[1], ray[0], ray[1]);
        }

        private static float[] pair(JsonObject json, String key, float low, float high) {
            if (!json.has(key))
                return new float[] { low, high };
            JsonArray pair = json.getAsJsonArray(key);
            return new float[] { pair.get(0).getAsFloat(), pair.get(1).getAsFloat() };
        }
    }

    public enum Blend {
        VANILLA, TINT
    }

    static GlintMaterial fromJson(JsonObject json) {
        return new GlintMaterial(
            json.has("blend") ? Blend.valueOf(json.get("blend").getAsString().toUpperCase(Locale.ROOT)) : Blend.VANILLA,
            json.has("texture") ? texture(json.get("texture").getAsString()) : null,
            Math.max(0, Math.min(1, get(json, "colorize", 0))), Math.max(0, Math.min(1, get(json, "strength", 1))),
            Math.max(0, get(json, "selective", 0)), json.has("ramp") ? Ramp.fromJson(json) : null,
            json.has("vanilla") && json.get("vanilla").getAsBoolean());
    }

    /** '#RRGGBB' (or 'RRGGBB') as red, green and blue from 0 to 1. */
    private static float[] rgb(String hex) {
        int rgb = Integer.parseInt(hex.replace("#", ""), 16);
        return new float[] { (rgb >> 16 & 255) / 255F, (rgb >> 8 & 255) / 255F, (rgb & 255) / 255F };
    }

    /** "ns:path/to/file.png" as given, or a bare name as {@code lifeguard:textures/glint/<name>.png}. */
    private static ResourceLocation texture(String value) {
        return value.contains(":") ? ResourceLocation.parse(value) : ownTexture(value);
    }

    /** Where a color's own texture lives in Lifeguard: {@code lifeguard:textures/glint/<name>.png}. */
    static ResourceLocation ownTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "textures/glint/" + name + ".png");
    }

    private static float get(JsonObject json, String key, float fallback) {
        return json.has(key) ? json.get(key).getAsFloat() : fallback;
    }
}
