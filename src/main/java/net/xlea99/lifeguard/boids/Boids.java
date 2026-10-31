package net.xlea99.lifeguard.boids;

import java.util.HashMap;
import java.util.List;
import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FollowFlockLeaderGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.xlea99.lifeguard.entry.boidsify.Boidsify;
import net.xlea99.lifeguard.mixin.boidsify.MobAccessor;
import org.slf4j.Logger;

/**
 * Fish school like real fish ({@link Boidsify}): a species listed in a datapack's {@code boidsify} folder (one file each,
 * see {@link BoidSpecies}) has its own schooling and wandering swapped for {@link BoidSchoolingGoal}. Nothing is listed by
 * default, so the entry alone changes no fish: a pack chooses its schools.
 *
 * <p>The swap is made as a fish enters the world, and redone for every loaded fish when the datapacks reload: a species
 * dropped from the files gets its own goals back. What's swapped out is whatever schooling or swimming-about the fish's
 * mod gave it: vanilla's follow-the-leader and random swimming, and any goal named like them (Rainbow Reef's are their
 * own classes); a land stroll is left alone, so an amphibious mob still walks about out of water. The boids goal takes the
 * slot of the highest of them, so whatever the fish's AI put above its schooling (panic, fleeing, food) still comes first.
 *
 * <p>Self-contained (this package, {@code mixin.boidsify} and the entry), to spin out into its own small mod one day.
 */
public final class Boids {

    private static final Logger LOGGER = LogUtils.getLogger();
    /** A mod's own schooling or wandering, by class name: follow-the-leader kinds and random swims. */
    private static final Pattern SCHOOLING = Pattern.compile("(?i).*(follow\\w*(leader|school|flock)|random\\w*swim|swim\\w*wander).*");

    private static volatile Map<EntityType<?>, BoidSpecies> species = Map.of();
    /** Species whose speeds were logged since the files last loaded (once each: for tuning). */
    private static final java.util.Set<EntityType<?>> LOGGED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private Boids() {
    }

    @Nullable
    static BoidSpecies species(EntityType<?> type) {
        return species.get(type);
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(Boids::onAddReloadListener);
        NeoForge.EVENT_BUS.addListener(Boids::onJoin);
    }

    /** A fish's boids goal, if it has one: how a fish sees its schoolmates' decisions. */
    @Nullable
    static BoidSchoolingGoal goalOf(Mob mob) {
        return installed(goals(mob));
    }

    /** Replaces every species, and swaps loaded fish over to match (the datapack loader). */
    static void setSpecies(Map<EntityType<?>, BoidSpecies> all) {
        species = Map.copyOf(all);
        LOGGED.clear();
        reconcileAll();
    }

    private static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new Loader());
    }

    private static void onJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Mob mob)
            reconcile(mob);
    }

    /** Swaps every loaded fish to match the files. */
    static void reconcileAll() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null)
            return;
        server.execute(() -> {
            for (ServerLevel level : server.getAllLevels())
                for (Entity entity : level.getAllEntities())
                    if (entity instanceof Mob mob)
                        reconcile(mob);
        });
    }

    /** Puts the boids goal in, or takes it out and gives the fish its own goals back, to match the files. */
    static void reconcile(Mob mob) {
        GoalSelector goals = goals(mob);
        BoidSchoolingGoal boids = installed(goals);
        BoidSpecies wanted = Boidsify.ENTRY.active() ? species.get(mob.getType()) : null;
        if (wanted != null && boids == null) {
            List<WrappedGoal> replaced = goals.getAvailableGoals()
                .stream()
                .filter(wrapped -> replaces(wrapped.getGoal()))
                .toList();
            int priority = wanted.priority() != null ? wanted.priority() : replaced.stream()
                .mapToInt(WrappedGoal::getPriority)
                .min()
                .orElse(4);
            for (WrappedGoal wrapped : replaced)
                goals.removeGoal(wrapped.getGoal());
            BoidSchoolingGoal added = new BoidSchoolingGoal(mob, replaced);
            goals.addGoal(priority, added);
            if (LOGGED.add(mob.getType()))
                introduce(mob, wanted, added);
        } else if (wanted == null && boids != null) {
            goals.removeGoal(boids);
            for (WrappedGoal wrapped : boids.replaced)
                goals.addGoal(wrapped.getPriority(), wrapped.getGoal());
        }
    }

    /** Says, once a species per load, what a pack maker tuning it should know: its speeds, and anything that won't work. */
    private static void introduce(Mob mob, BoidSpecies wanted, BoidSchoolingGoal added) {
        ResourceLocation id = EntityType.getKey(mob.getType());
        LOGGER.info("boidsify: {} swims {} blocks a tick on its own (moving); its schools swim {} to {}", id,
            String.format("%.3f", added.ownSpeed()), String.format("%.3f", added.ownSpeed() * wanted.minSpeed()),
            String.format("%.3f", added.ownSpeed() * wanted.maxSpeed()));
        if (added.wander == null)
            LOGGER.warn("boidsify: {} has no swimming-about goal boidsify knows, so alone or cooling down it keeps swimming by boids", id);
        if (wanted.sameVariant() && BoidVariants.of(mob) == null)
            LOGGER.warn("boidsify: {} is set to school only with its own variant, but its variant can't be read; it schools with all its kind", id);
    }

    /** Each class's isFollower() and stopFollowing(), if it has both (Rainbow Reef's schooling fish). */
    private static final Map<Class<?>, Optional<MethodHandle[]>> FOLLOWING = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Cuts a fish's follow-the-leader link (made when it spawned in a group), so its own wandering will run: vanilla's
     * through vanilla's schooling fish, any other mod's through a public {@code isFollower()} and {@code stopFollowing()}
     * on its class, found once.
     */
    static void unfollow(Mob mob) {
        if (mob instanceof AbstractSchoolingFish fish) {
            if (fish.isFollower())
                fish.stopFollowing();
            return;
        }
        Optional<MethodHandle[]> handles = FOLLOWING.computeIfAbsent(mob.getClass(), type -> {
            MethodHandle isFollower = BoidVariants.handle(type, "isFollower");
            MethodHandle stopFollowing = BoidVariants.handle(type, "stopFollowing");
            return isFollower != null && stopFollowing != null ? Optional.of(new MethodHandle[] {isFollower, stopFollowing}) : Optional.empty();
        });
        if (handles.isEmpty())
            return;
        try {
            if ((boolean) handles.get()[0].invoke(mob))
                handles.get()[1].invoke(mob);
        } catch (Throwable ignored) {
        }
    }

    private static GoalSelector goals(Mob mob) {
        return ((MobAccessor) mob).lifeguard$goalSelector();
    }

    @Nullable
    private static BoidSchoolingGoal installed(GoalSelector goals) {
        for (WrappedGoal wrapped : goals.getAvailableGoals())
            if (wrapped.getGoal() instanceof BoidSchoolingGoal boids)
                return boids;
        return null;
    }

    private static boolean replaces(Goal goal) {
        return goal instanceof FollowFlockLeaderGoal || goal instanceof RandomSwimmingGoal || SCHOOLING.matcher(goal.getClass()
            .getSimpleName())
            .matches();
    }

    /**
     * Reads the {@code boidsify} folders: a file that can't be read is logged and skipped, the rest still load. Files are
     * read in order of their ids, so when two name the same fish, the same one wins every time (and the log says so).
     */
    private static final class Loader extends SimpleJsonResourceReloadListener {

        Loader() {
            super(new Gson(), "boidsify");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            Map<EntityType<?>, BoidSpecies> read = new HashMap<>();
            Map<EntityType<?>, ResourceLocation> from = new HashMap<>();
            files.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    ResourceLocation file = entry.getKey();
                    try {
                        BoidSpecies one = BoidSpecies.parse(entry.getValue(), file.toString());
                        if (one == null) {
                            LOGGER.info("boidsify: {} is for a mob that isn't installed; skipped", file);
                            return;
                        }
                        ResourceLocation earlier = from.put(one.type(), file);
                        if (earlier != null)
                            LOGGER.warn("boidsify: {} and {} are both for {}; using {}", earlier, file, EntityType.getKey(one.type()), file);
                        read.put(one.type(), one);
                    } catch (RuntimeException e) {
                        LOGGER.error("boidsify: couldn't read {}: {}", file, e.getMessage());
                    }
                });
            LOGGER.info("boidsify: {} species school by boids", read.size());
            setSpecies(read);
        }

    }

}
