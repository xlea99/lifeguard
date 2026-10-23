package net.xlea99.lifeguard.core.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Just enough TOML writing for Lifeguard's config files: keys, table headers and plain values. */
final class Toml {
    private static final Pattern BARE_KEY = Pattern.compile("[A-Za-z0-9_-]+");

    private Toml() {
    }

    static String key(String key) {
        return BARE_KEY.matcher(key).matches() ? key : string(key);
    }

    static String tablePath(List<String> path) {
        List<String> parts = new ArrayList<>();
        for (String part : path)
            parts.add(key(part));
        return String.join(".", parts);
    }

    static String string(String text) {
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20 || c == 0x7f)
                        out.append(String.format("\\u%04x", (int) c));
                    else
                        out.append(c);
                }
            }
        }
        return out.append('"').toString();
    }

    /** A value read back from a file, written out again: kept keys can hold anything TOML can. */
    static String value(Object value) {
        if (value instanceof String text)
            return string(text);
        if (value instanceof Boolean || value instanceof Integer || value instanceof Long)
            return value.toString();
        if (value instanceof Double || value instanceof Float) {
            double number = ((Number) value).doubleValue();
            if (Double.isNaN(number))
                return "nan";
            if (Double.isInfinite(number))
                return number > 0 ? "inf" : "-inf";
            return value.toString();
        }
        if (value instanceof List<?> list) {
            List<String> items = new ArrayList<>();
            for (Object item : list)
                items.add(value(item));
            return "[" + String.join(", ", items) + "]";
        }
        if (value instanceof UnmodifiableConfig table) {
            List<String> items = new ArrayList<>();
            for (UnmodifiableConfig.Entry entry : table.entrySet())
                items.add(key(entry.getKey()) + " = " + value(entry.getRawValue()));
            return "{ " + String.join(", ", items) + " }";
        }
        // Dates and times print as TOML expects them.
        return value.toString();
    }
}
