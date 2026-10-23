package net.xlea99.lifeguard.core;

import java.util.Objects;

/**
 * A named setting inside one entry, like a Tweeter's range. Not a switch, and no profile ever changes it: it has one
 * default, which only an explicit value in the config file overrides.
 *
 * <p>Declared as its own constant and handed to {@link Entry#values}, so code reads it through a typed handle
 * ({@code MAX_SPEAKERS.get()}) and a typo is a compile error rather than a missing key at runtime.
 */
public abstract sealed class SubValue<T> permits SubValue.Int, SubValue.Decimal, SubValue.Bool {
    private final String name;
    private final T defaultValue;
    private volatile T value;
    private Entry owner;

    SubValue(String name, T defaultValue) {
        this.name = Objects.requireNonNull(name);
        this.defaultValue = Objects.requireNonNull(defaultValue);
        this.value = defaultValue;
    }

    public static Int ofInt(String name, int defaultValue, int min, int max) {
        return new Int(name, defaultValue, min, max);
    }

    public static Decimal ofDecimal(String name, double defaultValue, double min, double max) {
        return new Decimal(name, defaultValue, min, max);
    }

    public static Bool ofBool(String name, boolean defaultValue) {
        return new Bool(name, defaultValue);
    }

    public String name() {
        return name;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public T get() {
        return value;
    }

    /** Sets the value, refusing anything out of range. Called by the config loader. */
    public void set(T newValue) {
        if (!accepts(newValue))
            throw new IllegalArgumentException(describe() + " can't be " + newValue + " (" + rangeText() + ")");
        value = newValue;
    }

    public void reset() {
        value = defaultValue;
    }

    /**
     * Sets the value from something read off disk or the network, where the type isn't known in advance (TOML hands
     * back a Long for a whole number, a Double for a decimal). Returns why it couldn't, or null when it did; a value
     * that doesn't fit leaves this one unchanged.
     */
    public abstract String setFrom(Object raw);

    public abstract boolean accepts(T candidate);

    /** Human-readable bounds, e.g. {@code 0 to 4}. Empty for values with none. */
    public abstract String rangeText();

    public Entry owner() {
        return owner;
    }

    /** {@code lifeguard.entry.<entry id>.value.<name>}: the value's display name. */
    public String nameKey() {
        return owner.nameKey() + ".value." + name;
    }

    void attach(Entry entry) {
        if (owner != null)
            throw new IllegalStateException(describe() + " is already attached to " + owner.id());
        owner = entry;
    }

    String describe() {
        return owner == null ? "sub-value '" + name + "'" : owner.id() + "." + name;
    }

    public static final class Int extends SubValue<Integer> {
        private final int min;
        private final int max;

        Int(String name, int defaultValue, int min, int max) {
            super(name, defaultValue);
            this.min = min;
            this.max = max;
        }

        public int min() {
            return min;
        }

        public int max() {
            return max;
        }

        @Override
        public boolean accepts(Integer candidate) {
            return candidate != null && candidate >= min && candidate <= max;
        }

        @Override
        public String setFrom(Object raw) {
            if (!(raw instanceof Integer || raw instanceof Long))
                return "isn't a whole number";
            long candidate = ((Number) raw).longValue();
            if (candidate < min || candidate > max)
                return "is outside " + rangeText();
            set((int) candidate);
            return null;
        }

        @Override
        public String rangeText() {
            return min + " to " + max;
        }
    }

    public static final class Decimal extends SubValue<Double> {
        private final double min;
        private final double max;

        Decimal(String name, double defaultValue, double min, double max) {
            super(name, defaultValue);
            this.min = min;
            this.max = max;
        }

        public double min() {
            return min;
        }

        public double max() {
            return max;
        }

        @Override
        public boolean accepts(Double candidate) {
            return candidate != null && !candidate.isNaN() && candidate >= min && candidate <= max;
        }

        @Override
        public String setFrom(Object raw) {
            if (!(raw instanceof Number given))
                return "isn't a number";
            if (!accepts(given.doubleValue()))
                return "is outside " + rangeText();
            set(given.doubleValue());
            return null;
        }

        @Override
        public String rangeText() {
            return min + " to " + max;
        }
    }

    public static final class Bool extends SubValue<Boolean> {
        Bool(String name, boolean defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public boolean accepts(Boolean candidate) {
            return candidate != null;
        }

        @Override
        public String setFrom(Object raw) {
            if (!(raw instanceof Boolean given))
                return "isn't true or false";
            set(given);
            return null;
        }

        @Override
        public String rangeText() {
            return "";
        }
    }
}
