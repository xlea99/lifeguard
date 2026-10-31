package net.xlea99.lifeguard.boids;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;

/**
 * How one species schools ({@link Boids}), from a datapack file {@code data/<namespace>/boidsify/<any path>.json}.
 *
 * <p>Units: speeds are multiples of the fish's own swimming speed (1 swims at the pace its own AI swims); steering
 * forces are in blocks per tick per tick, against speeds of about 0.05 to 0.15 blocks a tick; times are seconds, angles
 * degrees, distances blocks. Every {@code chance} is per fish per second, and 0 turns that thing off. A range is
 * {@code [min, max]} (or one number for both).
 *
 * <p>Only {@code entity} is required. The core (speed, neighbors, flocking, walls, jitter, and the {@code turn} event) is
 * always on, at its defaults if left out. Everything else is flavor, and only there if its block is written: {@code dive},
 * {@code surge} and {@code pause} events, {@code peel_off}, {@code school.breakup} and {@code loner}. A short file is a
 * calm, plain school.
 *
 * <pre>
 * {
 *   "entity": "minecraft:cod",
 *   "same_variant": false,          true: it schools only with its own variant ({@link BoidVariants})
 *   "speed": [0.7, 1.4],            how slow and how fast it swims, times its own speed
 *   "acceleration": 0.1,            the most speed it gains a tick, times its own speed
 *   "deceleration": 0.1,            the most it loses a tick (left out: the same as acceleration)
 *   "neighbors": { "radius": 4, "count": 7, "vision": 360 },
 *                                   how far it looks for schoolmates, how many of the nearest it steers by, and its
 *                                   field of view in degrees (those outside it don't line it up or draw it in)
 *   "flocking": {
 *     "personal_space": 0.8,        closer than this, schoolmates push apart...
 *     "separation": 0.03,           ...this hard (felt all around, whatever the vision)
 *     "alignment": 0.08,            swims the way its schoolmates swim
 *     "cohesion": 0.004,            pulls toward their middle
 *     "heading": 0.004,             pulls along its own heading, which school decisions turn
 *     "max_force": 0.012            the most all of the above change its velocity in a tick
 *   },
 *   "wall_lookahead": 1.5,          how far ahead it watches for walls
 *   "jitter": { "turn": 0.03, "climb": 0.01 },   how much its heading wanders a tick, sideways and up-down (radians)
 *   "events": {                     school decisions: a fish makes one, and its schoolmates take it up, each with the
 *                                   event's spread chance a tick, so it ripples through the school
 *     "turn":  { "chance": 0.02, "spread": 0.25, "angle": [25, 120] },
 *     "dive":  { "chance": 0.005, "spread": 0.25, "angle": [10, 30] },     up or down, this steeply
 *     "surge": { "chance": 0.01, "spread": 0.25, "speed": [0.5, 1.8], "seconds": [0.6, 2] },
 *                                   faster (over 1) or slower (under 1) than its usual speed a while
 *     "pause": { "chance": 0.004, "spread": 0.08, "seconds": [1.5, 4], "look_around": 0.3, "stagger": 1.5 }
 *                                   the school stops a while: each fish a beat late (up to stagger seconds), now and
 *                                   then one breaks away, look_around of them turn to look for the others, and
 *                                   they set off again a few at a time (the rest within about twice stagger)
 *   },
 *   "peel_off": { "chance": 0.003, "seconds": [3, 10], "cooldown": [5, 15] },
 *                                   a fish swims off alone a while, then swims its own way (its own AI) before it
 *                                   schools again
 *   "school": {
 *     "max_size": 30,               a decision reaches at most this many fish, so bigger schools split at their next
 *                                   turn (left out: no limit)
 *     "breakup": { "min_size": 20, "chance": 0.01, "per_fish": 0.002, "split": 0.7 }
 *                                   a school of min_size or more may break up, more likely the bigger it is: split
 *                                   into 2 or 3 schools heading apart, or (the rest of the time) shatter loose
 *   },
 *   "loner": { "chance": 0.005, "spread": 0.25, "seconds": [8, 20] },
 *                                   a LONER species: it swims its own way, and now and then a fish starts a gathering
 *                                   that spreads to the loners around it, who school together, fast, a while
 *   "priority": 4                   its AI slot (left out: the slot of the schooling it replaces); taken when a fish
 *                                   loads, so a change needs the fish reloaded
 * }
 * </pre>
 */
public record BoidSpecies(EntityType<?> type, boolean sameVariant, float minSpeed, float maxSpeed, float acceleration,
    float deceleration, float neighborRadius, int neighborCount, float vision, float personalSpace, float separation,
    float alignment, float cohesion, float heading, float maxForce, float wallLookahead, float turnJitter,
    float climbJitter, Event turn, @Nullable Event dive, @Nullable Surge surge, @Nullable Pause pause,
    @Nullable PeelOff peelOff, int maxSchool, @Nullable Breakup breakup, @Nullable Loner loner,
    @Nullable Integer priority) {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** A school decision that turns its heading: how often, how readily it spreads, and how far (degrees). */
    public record Event(float chance, float spread, float minAngle, float maxAngle) {
    }

    /** A school decision to swim faster or slower a while: times its usual speed, for how long. */
    public record Surge(float chance, float spread, float minSpeed, float maxSpeed, float minSeconds, float maxSeconds) {
    }

    /** A school decision to stop a while, and how awkwardly each fish takes it. */
    public record Pause(float chance, float spread, float minSeconds, float maxSeconds, float lookAround, float stagger) {
    }

    /** A fish swimming off alone a while, then on its own AI a while before it schools again. */
    public record PeelOff(float chance, float minSeconds, float maxSeconds, float minCooldown, float maxCooldown) {
    }

    /** When a big school breaks up: from what size, how likely, and the share that split rather than shatter. */
    public record Breakup(int minSize, float chance, float perFish, float split) {
    }

    /** A loner species' gatherings: how often one starts, how readily it spreads, and how long it lasts. */
    public record Loner(float chance, float spread, float minSeconds, float maxSeconds) {
    }

    private static final Set<String> TOP = Set.of("entity", "same_variant", "speed", "acceleration", "deceleration", "neighbors",
        "flocking", "wall_lookahead", "jitter", "events", "peel_off", "school", "loner", "priority");

    /** The species a file describes, or null when its entity isn't installed (a file for a mod that isn't there). */
    @Nullable
    public static BoidSpecies parse(JsonElement json, String source) {
        JsonObject o = GsonHelper.convertToJsonObject(json, "boidsify species");
        known(o, TOP, source, "");
        ResourceLocation id = ResourceLocation.parse(GsonHelper.getAsString(o, "entity"));
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id))
            return null;
        float[] speed = range(o, "speed", 0.7F, 1.4F, 0.0F, 100.0F);
        if (speed[1] <= 0.0F)
            throw new JsonSyntaxException("speed must go above 0");
        float acceleration = positive(o, "acceleration", 0.1F);
        JsonObject neighbors = section(o, "neighbors", Set.of("radius", "count", "vision"), source);
        JsonObject flocking = section(o, "flocking", Set.of("personal_space", "separation", "alignment", "cohesion", "heading", "max_force"), source);
        JsonObject jitter = section(o, "jitter", Set.of("turn", "climb"), source);
        JsonObject events = section(o, "events", Set.of("turn", "dive", "surge", "pause"), source);
        JsonObject school = section(o, "school", Set.of("max_size", "breakup"), source);
        return new BoidSpecies(BuiltInRegistries.ENTITY_TYPE.get(id), GsonHelper.getAsBoolean(o, "same_variant", false), speed[0], speed[1],
            acceleration, o.has("deceleration") ? positive(o, "deceleration", acceleration) : acceleration,
            positive(neighbors, "radius", 4.0F), Math.max(1, GsonHelper.getAsInt(neighbors, "count", 7)),
            Mth.clamp(GsonHelper.getAsFloat(neighbors, "vision", 360.0F), 1.0F, 360.0F), atLeast0(flocking, "personal_space", 0.8F),
            atLeast0(flocking, "separation", 0.03F), atLeast0(flocking, "alignment", 0.08F), atLeast0(flocking, "cohesion", 0.004F),
            atLeast0(flocking, "heading", 0.004F), positive(flocking, "max_force", 0.012F), atLeast0(o, "wall_lookahead", 1.5F),
            atLeast0(jitter, "turn", 0.03F), atLeast0(jitter, "climb", 0.01F),
            event(section(events, "turn", Set.of("chance", "spread", "angle"), source), 0.02F, 0.25F, 25.0F, 120.0F),
            events.has("dive") ? event(section(events, "dive", Set.of("chance", "spread", "angle"), source), 0.005F, 0.25F, 10.0F, 30.0F) : null,
            events.has("surge") ? surge(section(events, "surge", Set.of("chance", "spread", "speed", "seconds"), source)) : null,
            events.has("pause") ? pause(section(events, "pause", Set.of("chance", "spread", "seconds", "look_around", "stagger"), source)) : null,
            o.has("peel_off") ? peelOff(section(o, "peel_off", Set.of("chance", "seconds", "cooldown"), source)) : null,
            Math.max(1, GsonHelper.getAsInt(school, "max_size", Integer.MAX_VALUE)),
            school.has("breakup") ? breakup(section(school, "breakup", Set.of("min_size", "chance", "per_fish", "split"), source)) : null,
            o.has("loner") ? loner(section(o, "loner", Set.of("chance", "spread", "seconds"), source)) : null,
            o.has("priority") ? GsonHelper.getAsInt(o, "priority") : null);
    }

    private static Event event(JsonObject o, float chance, float spread, float minAngle, float maxAngle) {
        float[] angle = range(o, "angle", minAngle, maxAngle, 0.0F, 180.0F);
        return new Event(atLeast0(o, "chance", chance), atLeast0(o, "spread", spread), angle[0], angle[1]);
    }

    private static Surge surge(JsonObject o) {
        float[] speed = range(o, "speed", 0.5F, 1.8F, 0.0F, 10.0F);
        float[] seconds = range(o, "seconds", 0.6F, 2.0F, 0.05F, 600.0F);
        return new Surge(atLeast0(o, "chance", 0.01F), atLeast0(o, "spread", 0.25F), speed[0], speed[1], seconds[0], seconds[1]);
    }

    private static Pause pause(JsonObject o) {
        float[] seconds = range(o, "seconds", 1.5F, 4.0F, 0.05F, 600.0F);
        return new Pause(atLeast0(o, "chance", 0.004F), atLeast0(o, "spread", 0.08F), seconds[0], seconds[1],
            Mth.clamp(GsonHelper.getAsFloat(o, "look_around", 0.3F), 0.0F, 1.0F), atLeast0(o, "stagger", 1.5F));
    }

    private static PeelOff peelOff(JsonObject o) {
        float[] seconds = range(o, "seconds", 3.0F, 10.0F, 0.05F, 600.0F);
        float[] cooldown = range(o, "cooldown", 5.0F, 15.0F, 0.0F, 600.0F);
        return new PeelOff(atLeast0(o, "chance", 0.003F), seconds[0], seconds[1], cooldown[0], cooldown[1]);
    }

    private static Breakup breakup(JsonObject o) {
        return new Breakup(Math.max(2, GsonHelper.getAsInt(o, "min_size", 20)), atLeast0(o, "chance", 0.01F), atLeast0(o, "per_fish", 0.002F),
            Mth.clamp(GsonHelper.getAsFloat(o, "split", 0.7F), 0.0F, 1.0F));
    }

    private static Loner loner(JsonObject o) {
        float[] seconds = range(o, "seconds", 8.0F, 20.0F, 0.05F, 600.0F);
        return new Loner(atLeast0(o, "chance", 0.005F), atLeast0(o, "spread", 0.25F), seconds[0], seconds[1]);
    }

    /** A {@code [min, max]} pair (in either order), or one number for both, kept within floor and ceiling. */
    private static float[] range(JsonObject o, String key, float min, float max, float floor, float ceiling) {
        if (!o.has(key))
            return new float[] {min, max};
        JsonElement value = o.get(key);
        float a;
        float b;
        if (value.isJsonArray()) {
            JsonArray pair = value.getAsJsonArray();
            if (pair.size() != 2)
                throw new JsonSyntaxException("'" + key + "' must be [min, max]");
            a = GsonHelper.convertToFloat(pair.get(0), key);
            b = GsonHelper.convertToFloat(pair.get(1), key);
        } else
            a = b = GsonHelper.convertToFloat(value, key);
        return new float[] {Mth.clamp(Math.min(a, b), floor, ceiling), Mth.clamp(Math.max(a, b), floor, ceiling)};
    }

    private static float atLeast0(JsonObject o, String key, float fallback) {
        return Math.max(0.0F, GsonHelper.getAsFloat(o, key, fallback));
    }

    private static float positive(JsonObject o, String key, float fallback) {
        float value = GsonHelper.getAsFloat(o, key, fallback);
        if (value <= 0.0F)
            throw new JsonSyntaxException("'" + key + "' must be above 0");
        return value;
    }

    /** A nested object (empty if it's left out), warning about keys it doesn't know: a typo would otherwise do nothing. */
    private static JsonObject section(JsonObject parent, String key, Set<String> keys, String source) {
        JsonObject o = GsonHelper.getAsJsonObject(parent, key, new JsonObject());
        known(o, keys, source, key + ".");
        return o;
    }

    private static void known(JsonObject o, Set<String> keys, String source, String path) {
        for (String key : o.keySet())
            if (!keys.contains(key))
                LOGGER.warn("boidsify: {} has '{}{}', which isn't a setting (a typo?); it does nothing", source, path, key);
    }

}
