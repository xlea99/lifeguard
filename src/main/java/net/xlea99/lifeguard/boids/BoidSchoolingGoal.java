package net.xlea99.lifeguard.boids;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FollowFlockLeaderGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.xlea99.lifeguard.entry.boidsify.Boidsify;
import net.xlea99.lifeguard.mixin.boidsify.RandomStrollGoalAccessor;

/**
 * A fish swimming as one of a school ({@link Boids}), by its {@link BoidSpecies}' numbers, read every tick (a /reload
 * retunes schools already swimming).
 *
 * <p><b>Steering.</b> Each tick it keeps its distance from its nearest schoolmates, swims their way and drifts toward
 * their middle (the last two only for schoolmates it can see), pulled gently along its own wandering heading. It turns
 * before walls, stays under the surface and off the floor, and gains and loses speed only so fast.
 *
 * <p><b>School events.</b> Now and then a fish makes a decision (turn, dive, surge, pause), and its schoolmates take it up
 * a few at a time, so it ripples through the school; a decision turns each fish that takes it toward its heading at a
 * fixed rate. A pause is taken awkwardly, each fish its own way. A decision reaches at most a school's biggest size, so
 * bigger schools split at turns, and a big school may break up.
 *
 * <p><b>Who's schooling.</b> Boids steer a fish only while it's in a school or swimming off from one. A fish alone (a
 * schooler with nobody in reach, a loner between gatherings) or cooling down after peeling off swims its own way: its
 * own wandering, the very goal this replaced, so it moves as its own AI moves it.
 *
 * <p>It takes only the movement flag: anything more urgent in the fish's own AI (panic, fleeing a player, food) still
 * takes over, which scatters a school, and the school forms again after. Its velocity is applied after the fish's own
 * movement code each tick ({@link SteeredMob}), so nothing of vanilla's (a fish's float) skews it.
 */
final class BoidSchoolingGoal extends Goal {

    // The feel that's fixed by design: taste worth naming, not worth a setting.
    /** How long a decision stays new enough to pass on (ticks); its heading and speed are followed twice as long. */
    private static final int FRESH = 60;
    private static final int FOLLOWED = FRESH * 2;
    /** How fast a decision turns a fish toward its heading, radians a tick (90 degrees in about a second and a half). */
    private static final float DECISION_TURN = 0.05F;
    /** How quickly a fish's pace moves toward a decision's, a tick. */
    private static final float PACE_EASE = 0.05F;
    /** How hard a fish steers toward the speed it wants: usually, and in a surge. */
    private static final double SPEED_STEER = 0.05;
    private static final double SURGE_STEER = 0.12;
    /** A paused fish's top speed, times its usual. */
    private static final double HOLD_SPEED = 0.15;
    /** How hard a fish brakes into a pause: each its own, from this range. */
    private static final float HOLD_EASE_MIN = 0.03F;
    private static final float HOLD_EASE_MAX = 0.15F;
    /** The share of a pause's fish that break away instead, and that set off on time when it ends. */
    private static final float BREAKAWAY = 0.08F;
    private static final float EAGER = 0.25F;
    /** The chance a tick that a fish still waiting out a pause notices a schoolmate swim off, and follows. */
    private static final float NOTICE = 0.06F;
    /** Swimming off: how far it turns away (degrees), and for how long when it isn't a peel-off (seconds). */
    private static final float AWAY_MIN = 60.0F;
    private static final float AWAY_MAX = 150.0F;
    private static final float BREAKAWAY_MIN = 3.0F;
    private static final float BREAKAWAY_MAX = 8.0F;
    /** A shattered school: how long it stays loose (seconds), and its top pace as it dawdles. */
    private static final float SHATTER_MIN = 4.0F;
    private static final float SHATTER_MAX = 12.0F;
    private static final float DAWDLE = 0.4F;
    /** A split: how far apart its new schools head (degrees). */
    private static final float SPLIT_MIN = 45.0F;
    private static final float SPLIT_MAX = 60.0F;
    /** A gathering's pace: the fast end of the range. */
    private static final float GATHER_PACE = 0.75F;
    /** Ticks a schooling fish goes with nobody in reach before it swims its own way. */
    private static final int LONELY = 60;
    /** Ways out of a wall, degrees from straight ahead, nearest first. */
    private static final int[] TURNS = {30, -30, 60, -60, 90, -90, 135, -135};
    /** "Never" (and "no end"): far enough back that any real time is later, near enough not to overflow. */
    private static final long NEVER = -1_000_000L;

    /** What a fish is doing, and how the school pulls on it. */
    enum Mood {
        SCHOOLING(true, true, 1.0, 1.0, 1.0, 1.0, 1.0F),
        /** Swimming off alone (a peel-off, or a breakaway from a pause): its own heading, no pull from the school. */
        SOLO(true, false, 0.0, 0.0, 1.0, 3.0, 2.0F),
        /** Its school shattered: barely lined up, not drawn in, more room. */
        LOOSE(true, true, 0.15, 0.0, 2.0, 2.0, 2.0F),
        /** Back from swimming off, not ready for a school: its own AI. */
        COOLDOWN(false, false, 0.0, 0.0, 1.0, 2.0, 2.0F),
        /** Alone (a loner between gatherings, or a schooler with nobody in reach): its own AI. */
        ALONE(false, false, 0.0, 0.0, 1.0, 2.0, 2.0F);

        /** Boids steer it (else its own AI does, when it has one). */
        final boolean boids;
        /** Schoolmates line up with it and are drawn to it. */
        final boolean drawsSchool;
        final double align;
        final double cohere;
        final double room;
        final double heading;
        final float jitter;

        Mood(boolean boids, boolean drawsSchool, double align, double cohere, double room, double heading, float jitter) {
            this.boids = boids;
            this.drawsSchool = drawsSchool;
            this.align = align;
            this.cohere = cohere;
            this.room = room;
            this.heading = heading;
            this.jitter = jitter;
        }
    }

    /** What a decision is: it decides which spread chance it travels at. */
    enum Kind {
        TURN,
        DIVE,
        SURGE,
        PAUSE,
        GATHER
    }

    /**
     * A decision: a heading, a pace and maybe a surge or a pause, made by one fish at one time, and the fish that took it
     * up (its maker first). Those fish are, as near as anything, its school: how big a school is, who a breakup moves.
     */
    static final class Decision {

        final Kind kind;
        final long at;
        final float yaw;
        final float pitch;
        final float pace;
        final List<BoidSchoolingGoal> members = new ArrayList<>();
        /** How many of its members follow it right now. */
        int live;
        float surge = 1.0F;
        long surgeUntil = NEVER;
        long holdUntil = NEVER;
        /** A school breaks up once per decision, at most. */
        boolean brokenUp;

        Decision(Kind kind, long at, float yaw, float pitch, float pace) {
            this.kind = kind;
            this.at = at;
            this.yaw = yaw;
            this.pitch = pitch;
            this.pace = pace;
        }

        BoidSchoolingGoal maker() {
            return this.members.get(0);
        }

        /** When it's spent: its heading followed long enough, and any surge or pause over. */
        long endsAt() {
            return Math.max(this.at + FOLLOWED, Math.max(this.surgeUntil, this.holdUntil));
        }

    }

    private final Mob mob;
    private final EntityType<?> type;
    /** The fish's own schooling and wandering this replaced, given back if it's taken off ({@link Boids}). */
    final List<WrappedGoal> replaced;
    /** The fish's own wandering (one of those), run while it swims its own way; null if it had none we know. */
    @Nullable
    final Goal wander;
    private final RandomSource random;
    /** How fast its own wandering swam it, as a multiple of its speed (1 if it had none we know). */
    private final double wanderSpeed;
    /** How quick off the mark it is next to its species, 0.7 to 1.3; and its own lean within a school's pace. */
    private final float quickness;
    private final float bias;

    /** Where in its species' speed range it swims, 0 to 1. */
    private float pace;
    /** The velocity it steers by (and its schoolmates line up with), as set, not as dragged by the water. */
    Vec3 velocity = Vec3.ZERO;
    /** The heading it wanders along, in radians: yaw as Minecraft's, pitch upward. */
    private float headingYaw;
    private float headingPitch;
    private List<Mob> neighbors = new ArrayList<>();
    /** Each neighbor's goal, or null, in the neighbors' order. */
    private List<BoidSchoolingGoal> neighborGoals = new ArrayList<>();
    // Every fish's clocks start somewhere random, so no school ever does anything in step.
    private int refreshIn;
    private int bumpedFor;
    private int corneredFor;
    /** Since when it's had nobody in reach (NEVER: it has company). */
    private long lonelySince = NEVER;
    private boolean ownWay;
    private boolean wandering;

    @Nullable
    Decision decision;
    Mood mood = Mood.SCHOOLING;
    /** When its mood runs out (game time); NEVER: it doesn't. */
    private long moodUntil = NEVER;
    /** A loner's gathering: when it began and ends (game time). */
    long gatherAt = NEVER;
    long gatherUntil = NEVER;
    /** The pause it took, and how: when it stops, when it sets off again, how hard it brakes, where it looks. */
    @Nullable
    Decision paused;
    private long holdFrom;
    long holdEnd;
    private float holdEase;
    private float lookAround = Float.NaN;

    BoidSchoolingGoal(Mob mob, List<WrappedGoal> replaced) {
        this.mob = mob;
        this.type = mob.getType();
        this.replaced = replaced;
        this.random = mob.getRandom();
        this.wander = replaced.stream()
            .map(WrappedGoal::getGoal)
            .filter(goal -> !(goal instanceof FollowFlockLeaderGoal) && !goal.getClass()
                .getSimpleName()
                .toLowerCase(Locale.ROOT)
                .contains("follow"))
            .findFirst()
            .orElse(null);
        this.wanderSpeed = replaced.stream()
            .filter(wrapped -> wrapped.getGoal() instanceof RandomStrollGoal)
            .mapToDouble(wrapped -> ((RandomStrollGoalAccessor) wrapped.getGoal()).lifeguard$speedModifier())
            .max()
            .orElse(1.0);
        this.quickness = 0.7F + this.random.nextFloat() * 0.6F;
        this.bias = (this.random.nextFloat() - 0.5F) * 0.3F;
        this.pace = this.random.nextFloat();
        this.refreshIn = this.random.nextInt(10);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    /**
     * How fast the fish swims on its own, in blocks a tick: the speed vanilla's water movement settles at, for its speed
     * and its own wandering's (a vanilla fish adds 1% of it a tick against 10% drag; other swimmers 2% against 20%,
     * times Forge's swim speed). A species' speeds are multiples of this.
     */
    double ownSpeed() {
        double push = Math.min(1.0, this.wanderSpeed * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
        if (this.mob instanceof AbstractFish)
            return 0.1 * push;
        AttributeInstance swim = this.mob.getAttribute(NeoForgeMod.SWIM_SPEED);
        return 0.1 * push * (swim == null ? 1.0 : swim.getValue());
    }

    @Nullable
    private BoidSpecies species() {
        return Boids.species(this.type);
    }

    @Override
    public boolean canUse() {
        return Boidsify.ENTRY.active() && this.species() != null && this.mob.isInWater();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.mob.getNavigation()
            .stop();
        this.headingYaw = this.mob.getYRot() * Mth.DEG_TO_RAD;
        this.headingPitch = 0.0F;
        this.velocity = this.mob.getDeltaMovement();
        this.refreshIn = 0;
    }

    @Override
    public void stop() {
        if (this.wandering && this.wander != null)
            this.wander.stop();
        this.wandering = false;
        this.ownWay = false;
        this.follow(null);
        this.paused = null;
        this.neighbors = new ArrayList<>();
        this.neighborGoals = new ArrayList<>();
    }

    @Override
    public void tick() {
        BoidSpecies s = this.species();
        if (s == null)
            return;
        long now = this.mob.level()
            .getGameTime();
        Vec3 pos = this.mob.getBoundingBox()
            .getCenter();
        // A spent decision is let go, so its list of fish (some long gone) goes with it.
        if (this.decision != null && now >= this.decision.endsAt() && !(this.paused == this.decision && now < this.holdEnd))
            this.follow(null);
        if (this.moodUntil != NEVER && now >= this.moodUntil)
            this.moodEnded(s, now);
        // Swimming off or cooling down, it pays the others no mind.
        if (this.mood != Mood.SOLO && this.mood != Mood.COOLDOWN && --this.refreshIn <= 0) {
            this.refreshIn = 10 + this.random.nextInt(10);
            this.refresh(pos, s, now);
        }
        this.settle(s, now);
        Decision before = this.decision;
        boolean holding = this.holding(s, now);
        this.roll(s, now, holding);
        this.takeUp(s, now, holding);
        // A pause it just made or took up holds it from now.
        if (this.decision != before)
            holding = this.holding(s, now);
        if (!this.mood.boids && this.wander != null) {
            this.swimOwnWay();
            return;
        }
        if (this.ownWay)
            this.backToSchooling();
        this.steer(s, now, pos, holding);
    }

    // --- Who it's with

    /** Its nearest kind (its variant too, if its species asks), and whether any of them is company. */
    private void refresh(Vec3 pos, BoidSpecies s, long now) {
        this.neighbors = this.nearest(pos, s.neighborRadius(), s.neighborCount(), s.sameVariant());
        this.neighborGoals = new ArrayList<>(this.neighbors.size());
        boolean company = false;
        for (Mob other : this.neighbors) {
            BoidSchoolingGoal goal = Boids.goalOf(other);
            this.neighborGoals.add(goal);
            company |= goal == null || goal.mood.drawsSchool || goal.mood == Mood.ALONE;
        }
        if (company)
            this.lonelySince = NEVER;
        else if (this.lonelySince == NEVER)
            this.lonelySince = now;
        // A schooler alone, meeting its kind, schools with them.
        if (company && this.mood == Mood.ALONE && s.loner() == null)
            this.rest(s, now);
    }

    /**
     * Keeps its mood true to its species and company: a loner between gatherings swims alone (the species may have
     * turned loner in a /reload), and a schooler with nobody in reach a while swims its own way until it meets some.
     */
    private void settle(BoidSpecies s, long now) {
        if (this.moodUntil != NEVER)
            return;
        if (this.mood == Mood.SCHOOLING && s.loner() != null && now >= this.gatherUntil
            || this.mood == Mood.SCHOOLING && s.loner() == null && this.lonelySince != NEVER && now - this.lonelySince > LONELY) {
            this.mood = Mood.ALONE;
            this.follow(null);
        }
    }

    private void moodEnded(BoidSpecies s, long now) {
        BoidSpecies.PeelOff peel = s.peelOff();
        if (this.mood == Mood.SOLO && peel != null && peel.maxCooldown() > 0.0F) {
            this.mood = Mood.COOLDOWN;
            this.moodUntil = now + this.ticks(peel.minCooldown(), peel.maxCooldown());
        } else
            this.rest(s, now);
    }

    /** Back to its resting mood: schooling (or, a loner, alone, unless its gathering is still on). */
    private void rest(BoidSpecies s, long now) {
        this.lonelySince = NEVER;
        if (s.loner() != null && now >= this.gatherUntil) {
            this.mood = Mood.ALONE;
            this.moodUntil = NEVER;
        } else {
            this.mood = Mood.SCHOOLING;
            this.moodUntil = s.loner() != null ? this.gatherUntil : NEVER;
        }
    }

    // --- Decisions

    private long decidedAt() {
        return this.decision == null ? NEVER : this.decision.at;
    }

    private void follow(@Nullable Decision next) {
        if (this.decision == next)
            return;
        if (this.decision != null)
            this.decision.live--;
        this.decision = next;
        if (next != null) {
            next.live++;
            next.members.add(this);
        }
    }

    private Decision decide(Kind kind, long now, float yaw, float pitch, float pace) {
        Decision made = new Decision(kind, now, yaw, pitch, pace);
        this.follow(made);
        return made;
    }

    /** The heading a new decision starts from: the one it's turning to, if it's mid-turn. */
    private float aimYaw() {
        return this.decision != null ? this.decision.yaw : this.headingYaw;
    }

    /** Its own rolls: a schooling fish's decisions, peel-off and breakup; a loner's gathering. */
    private void roll(BoidSpecies s, long now, boolean holding) {
        BoidSpecies.Loner loner = s.loner();
        if (this.mood == Mood.ALONE && loner != null && this.chance(loner.chance())) {
            this.gatherAt = now;
            this.gatherUntil = now + this.ticks(loner.minSeconds(), loner.maxSeconds());
            this.decide(Kind.GATHER, now, this.headingYaw, 0.0F, GATHER_PACE + this.random.nextFloat() * (1.0F - GATHER_PACE));
            this.rest(s, now);
            return;
        }
        if (this.mood != Mood.SCHOOLING || holding)
            return;
        BoidSpecies.Event turn = s.turn();
        if (this.chance(turn.chance())) {
            float angle = Mth.lerp(this.random.nextFloat(), turn.minAngle(), turn.maxAngle()) * Mth.DEG_TO_RAD;
            this.decide(Kind.TURN, now, this.aimYaw() + (this.random.nextBoolean() ? angle : -angle), this.headingPitch, this.pace);
        }
        BoidSpecies.Event dive = s.dive();
        if (dive != null && this.chance(dive.chance())) {
            float angle = Mth.lerp(this.random.nextFloat(), dive.minAngle(), dive.maxAngle()) * Mth.DEG_TO_RAD;
            this.decide(Kind.DIVE, now, this.aimYaw(), this.random.nextBoolean() ? angle : -angle, this.pace);
        }
        BoidSpecies.Surge surge = s.surge();
        if (surge != null && this.chance(surge.chance())) {
            Decision made = this.decide(Kind.SURGE, now, this.aimYaw(), this.headingPitch, this.pace);
            made.surge = Mth.lerp(this.random.nextFloat(), surge.minSpeed(), surge.maxSpeed());
            made.surgeUntil = now + this.ticks(surge.minSeconds(), surge.maxSeconds());
        }
        BoidSpecies.Pause pause = s.pause();
        if (pause != null && this.chance(pause.chance()))
            this.decide(Kind.PAUSE, now, this.aimYaw(), 0.0F, this.pace).holdUntil = now + this.ticks(pause.minSeconds(), pause.maxSeconds());
        BoidSpecies.PeelOff peel = s.peelOff();
        if (peel != null && !this.neighbors.isEmpty() && this.chance(peel.chance()))
            this.swimOff(now, peel.minSeconds(), peel.maxSeconds());
        else
            this.maybeBreakUp(s, now);
    }

    /**
     * Takes up schoolmates' newer decisions, each kind at its own spread chance a tick, while they're new (a pause for
     * its whole hold), and while they still have room (a school's biggest size). A fish waiting out a pause takes up
     * nothing; a fish alone takes up only a loner's gathering.
     */
    private void takeUp(BoidSpecies s, long now, boolean holding) {
        if (this.mood == Mood.SOLO || this.mood == Mood.COOLDOWN || holding)
            return;
        for (BoidSchoolingGoal mate : this.neighborGoals) {
            Decision theirs = mate == null ? null : mate.decision;
            if (theirs == null || theirs.at <= this.decidedAt() || theirs.live >= s.maxSchool()
                || now - theirs.at >= FRESH && now >= theirs.holdUntil || this.random.nextFloat() >= spread(s, theirs.kind))
                continue;
            if (theirs.kind == Kind.GATHER) {
                if (s.loner() == null || now >= mate.gatherUntil)
                    continue;
                this.gatherAt = mate.gatherAt;
                this.gatherUntil = mate.gatherUntil + this.random.nextInt(40);
                this.follow(theirs);
                this.rest(s, now);
            } else if (this.mood != Mood.ALONE)
                this.follow(theirs);
        }
    }

    private static float spread(BoidSpecies s, Kind kind) {
        return switch (kind) {
            case TURN -> s.turn()
                .spread();
            case DIVE -> s.dive() != null ? s.dive()
                .spread() : 0.0F;
            case SURGE -> s.surge() != null ? s.surge()
                .spread() : 0.0F;
            case PAUSE -> s.pause() != null ? s.pause()
                .spread() : 0.0F;
            case GATHER -> s.loner() != null ? s.loner()
                .spread() : 0.0F;
        };
    }

    /** Swims off alone a while: away from its heading, dropping the school's decision (else it would steer it back). */
    private void swimOff(long now, float minSeconds, float maxSeconds) {
        this.follow(null);
        this.paused = null;
        this.mood = Mood.SOLO;
        this.moodUntil = now + this.ticks(minSeconds, maxSeconds);
        float away = Mth.lerp(this.random.nextFloat(), AWAY_MIN, AWAY_MAX) * Mth.DEG_TO_RAD;
        this.headingYaw += this.random.nextBoolean() ? away : -away;
    }

    /**
     * The fish that made its school's decision rolls, while it's fresh, for the school to break up: no chance below the
     * species' smallest size for it, more for every fish above. A breakup splits the school (fish on far sides of it each
     * set off a new way, fanned apart, and the school divides by whichever reaches each fish first) or shatters it (all of
     * it goes loose at once).
     */
    private void maybeBreakUp(BoidSpecies s, long now) {
        BoidSpecies.Breakup breakup = s.breakup();
        Decision ours = this.decision;
        if (breakup == null || ours == null || ours.brokenUp || ours.maker() != this || now - ours.at >= FOLLOWED
            || ours.live < breakup.minSize() || !this.chance(breakup.chance() + breakup.perFish() * (ours.live - breakup.minSize())))
            return;
        List<BoidSchoolingGoal> school = ours.members.stream()
            .filter(member -> member.mob.isAlive() && member.decision == ours && member.mood == Mood.SCHOOLING)
            .distinct()
            .toList();
        if (school.size() < breakup.minSize())
            return;
        ours.brokenUp = true;
        if (this.random.nextFloat() >= breakup.split()) {
            long until = now + this.ticks(SHATTER_MIN, SHATTER_MAX);
            for (BoidSchoolingGoal member : school)
                member.loosen(until + this.random.nextInt(40));
            return;
        }
        int parts = Math.min(school.size(), 2 + this.random.nextInt(2));
        // Seeds as far apart as the school allows: its maker, then each time the fish furthest from every seed so far.
        List<BoidSchoolingGoal> seeds = new ArrayList<>(List.of(this));
        while (seeds.size() < parts) {
            BoidSchoolingGoal furthest = null;
            double best = -1.0;
            for (BoidSchoolingGoal member : school) {
                double nearestSeed = Double.MAX_VALUE;
                for (BoidSchoolingGoal seed : seeds)
                    nearestSeed = Math.min(nearestSeed, member.mob.distanceToSqr(seed.mob));
                if (nearestSeed > best) {
                    best = nearestSeed;
                    furthest = member;
                }
            }
            if (furthest == null || best <= 0.0)
                break;
            seeds.add(furthest);
        }
        float fan = Mth.lerp(this.random.nextFloat(), SPLIT_MIN, SPLIT_MAX) * Mth.DEG_TO_RAD;
        for (int i = 0; i < seeds.size(); i++)
            seeds.get(i)
                .decide(Kind.TURN, now, ours.yaw + (i - (seeds.size() - 1) / 2.0F) * fan, ours.pitch, this.random.nextFloat());
    }

    /** A shattered school: barely lined up, not drawn together, dawdling, until then. */
    private void loosen(long until) {
        this.follow(null);
        this.paused = null;
        this.mood = Mood.LOOSE;
        this.moodUntil = until;
        this.pace = this.random.nextFloat() * DAWDLE;
    }

    // --- Pauses

    /**
     * Whether it's hanging still in a pause right now. The first time it sees a pause it takes it its own way: it breaks
     * away, or it stops a beat late, braking at its own rate, maybe turning to look for the others; then it sets off on
     * time (the eager few) or once it sees a schoolmate go (or it's waited long enough).
     */
    private boolean holding(BoidSpecies s, long now) {
        Decision pause = this.decision;
        BoidSpecies.Pause p = s.pause();
        if (pause == null || pause.holdUntil == NEVER || p == null || this.mood != Mood.SCHOOLING)
            return false;
        if (this.paused != pause) {
            if (now >= pause.holdUntil)
                return false;
            this.paused = pause;
            if (pause.maker() != this && this.random.nextFloat() < BREAKAWAY) {
                this.swimOff(now, BREAKAWAY_MIN, BREAKAWAY_MAX);
                return false;
            }
            this.holdFrom = now + Math.round(20.0F * this.random.nextFloat() * p.stagger());
            boolean eager = pause.maker() == this || this.random.nextFloat() < EAGER;
            this.holdEnd = pause.holdUntil + (eager ? this.random.nextInt(10) : Math.round(20.0F * p.stagger() * (1.0F + this.random.nextFloat())));
            this.holdEase = Mth.lerp(this.random.nextFloat(), HOLD_EASE_MIN, HOLD_EASE_MAX);
            float turn = Mth.lerp(this.random.nextFloat(), 90.0F, 180.0F) * Mth.DEG_TO_RAD;
            this.lookAround = this.random.nextFloat() < p.lookAround() ? this.headingYaw + (this.random.nextBoolean() ? turn : -turn) : Float.NaN;
        }
        if (now >= pause.holdUntil && now < this.holdEnd)
            for (BoidSchoolingGoal mate : this.neighborGoals)
                if (mate != null && mate.paused == pause && now >= mate.holdEnd && this.random.nextFloat() < NOTICE) {
                    this.holdEnd = Math.min(this.holdEnd, now + 1 + this.random.nextInt(10));
                    break;
                }
        return now >= this.holdFrom && now < this.holdEnd;
    }

    // --- Its own way

    /**
     * Runs the fish's own wandering as its goal selector would (asked to start every other tick, ticked while it runs),
     * after cutting any follow-the-leader link its spawn gave it: vanilla's wandering won't run for a fish that thinks it
     * follows a leader. Its heading and velocity follow along, ready for when it schools again.
     */
    private void swimOwnWay() {
        this.ownWay = true;
        this.headingYaw = this.mob.getYRot() * Mth.DEG_TO_RAD;
        this.velocity = this.mob.getDeltaMovement();
        Goal own = this.wander;
        if (this.wandering) {
            if (own.canContinueToUse())
                own.tick();
            else {
                own.stop();
                this.wandering = false;
            }
        } else if ((this.mob.tickCount & 1) == 0) {
            Boids.unfollow(this.mob);
            if (own.canUse()) {
                own.start();
                this.wandering = true;
            }
        }
    }

    /** Takes over from the fish's own wandering, picking up from however it was swimming. */
    private void backToSchooling() {
        if (this.wandering && this.wander != null)
            this.wander.stop();
        this.wandering = false;
        this.ownWay = false;
        this.mob.getNavigation()
            .stop();
        this.velocity = this.mob.getDeltaMovement();
        this.headingYaw = this.mob.getYRot() * Mth.DEG_TO_RAD;
        this.headingPitch = 0.0F;
    }

    // --- Steering

    private void steer(BoidSpecies s, long now, Vec3 pos, boolean holding) {
        double own = this.ownSpeed();
        double minSpeed = s.minSpeed() * own;
        double maxSpeed = s.maxSpeed() * own;
        Decision following = this.decision;
        float surge = !holding && following != null && now < following.surgeUntil ? following.surge : 1.0F;
        if (holding) {
            minSpeed = 0.0;
            maxSpeed *= HOLD_SPEED;
        } else if (surge > 1.0F)
            maxSpeed *= surge;
        else
            minSpeed *= surge;
        if (following != null && !holding) {
            this.headingYaw = turnToward(this.headingYaw, following.yaw, DECISION_TURN);
            this.headingPitch = turnToward(this.headingPitch, following.pitch, DECISION_TURN);
            if (this.mood != Mood.LOOSE)
                this.pace += (Mth.clamp(following.pace + this.bias, 0.0F, 1.0F) - this.pace) * PACE_EASE;
        }
        this.headingYaw += (float) this.random.nextGaussian() * s.turnJitter() * this.mood.jitter;
        float steepest = Math.max(0.35F, s.dive() != null ? s.dive()
            .maxAngle() * Mth.DEG_TO_RAD : 0.0F);
        this.headingPitch = Mth.clamp(this.headingPitch * 0.98F + (float) this.random.nextGaussian() * s.climbJitter(), -steepest, steepest);

        Vec3 v = this.velocity;
        if (this.bumpedFor > 0)
            this.bumpedFor--;
        else if (this.mob.horizontalCollision) {
            // Ran into something the look-ahead missed (a corner, a block edge): turn well away, somewhere random.
            float turn = (90.0F + this.random.nextFloat() * 90.0F) * (this.random.nextBoolean() ? 1 : -1) * Mth.DEG_TO_RAD;
            v = v.yRot(turn);
            this.headingYaw = yawOf(v);
            this.bumpedFor = 10;
        }

        // Its schoolmates, in plain numbers (this runs for every fish every tick).
        double room = s.personalSpace() * this.mood.room;
        double reach = s.neighborRadius() * 1.5;
        boolean blinkered = s.vision() < 360.0F;
        double seen = Math.cos(s.vision() * 0.5 * Mth.DEG_TO_RAD);
        double speed0 = v.length();
        double faceX;
        double faceY;
        double faceZ;
        if (speed0 > 1.0E-4) {
            faceX = v.x / speed0;
            faceY = v.y / speed0;
            faceZ = v.z / speed0;
        } else {
            Vec3 d = direction(this.headingYaw, this.headingPitch);
            faceX = d.x;
            faceY = d.y;
            faceZ = d.z;
        }
        double sx = 0.0;
        double sy = 0.0;
        double sz = 0.0;
        double mx = 0.0;
        double my = 0.0;
        double mz = 0.0;
        double fx = 0.0;
        double fy = 0.0;
        double fz = 0.0;
        int count = 0;
        for (int i = 0; i < this.neighbors.size(); i++) {
            Mob other = this.neighbors.get(i);
            if (!other.isAlive())
                continue;
            AABB box = other.getBoundingBox();
            double ox = (box.minX + box.maxX) * 0.5;
            double oy = (box.minY + box.maxY) * 0.5;
            double oz = (box.minZ + box.maxZ) * 0.5;
            double ax = pos.x - ox;
            double ay = pos.y - oy;
            double az = pos.z - oz;
            double distance = Math.sqrt(ax * ax + ay * ay + az * az);
            if (distance > reach)
                continue;
            if (distance < room && distance > 1.0E-4) {
                double push = (room - distance) / (room * distance) * s.separation();
                sx += ax * push;
                sy += ay * push;
                sz += az * push;
            }
            // Only schooling fish pull a school along: one off on its own is given room, not swallowed.
            BoidSchoolingGoal theirs = this.neighborGoals.get(i);
            if (theirs != null && !theirs.mood.drawsSchool)
                continue;
            if (blinkered && distance > 1.0E-4 && -(ax * faceX + ay * faceY + az * faceZ) / distance < seen)
                continue;
            count++;
            mx += ox;
            my += oy;
            mz += oz;
            Vec3 their = theirs != null ? theirs.velocity : other.getDeltaMovement();
            fx += their.x;
            fy += their.y;
            fz += their.z;
        }
        if (count > 0) {
            double align = s.alignment() * this.mood.align;
            double cohere = s.cohesion() * this.mood.cohere;
            sx += (fx / count - v.x) * align + (mx / count - pos.x) * cohere;
            sy += (fy / count - v.y) * align + (my / count - pos.y) * cohere;
            sz += (fz / count - v.z) * align + (mz / count - pos.z) * cohere;
            // Between decisions, its heading leans the way its schoolmates swim, so a school keeps one heading.
            if (this.mood == Mood.SCHOOLING && following == null && fx * fx + fz * fz > 1.0E-8)
                this.headingYaw = turnToward(this.headingYaw, (float) Mth.atan2(-fx, fz), 0.01F);
        }
        // Paused, only a fish looking around turns (slowly); the rest just hang there.
        if (!holding || !Float.isNaN(this.lookAround)) {
            Vec3 ahead = holding ? direction(this.lookAround, 0.0F) : direction(this.headingYaw, this.headingPitch);
            double pull = holding ? s.heading() * 2.0 : s.heading() * this.mood.heading;
            sx += ahead.x * pull;
            sy += ahead.y * pull;
            sz += ahead.z * pull;
        }
        if (speed0 > 1.0E-4) {
            double preferred = holding ? 0.0 : surge > 1.0F ? maxSpeed : surge < 1.0F ? minSpeed : minSpeed + (maxSpeed - minSpeed) * this.pace;
            double rate = (preferred - speed0) / speed0 * (holding ? this.holdEase : surge != 1.0F ? SURGE_STEER : SPEED_STEER);
            sx += v.x * rate;
            sy += v.y * rate;
            sz += v.z * rate;
        }
        double force = Math.sqrt(sx * sx + sy * sy + sz * sz);
        if (force > s.maxForce()) {
            double cut = s.maxForce() / force;
            sx *= cut;
            sy *= cut;
            sz *= cut;
        }
        v = new Vec3(v.x + sx, v.y + sy, v.z + sz);
        // A decision turns it toward its heading at a fixed rate, whatever the steering above.
        if (following != null && !holding)
            v = rotateToward(v, following.yaw, following.pitch, DECISION_TURN);
        v = this.avoidWalls(pos, v, s, now, holding);

        // Within its speeds, but it gains and loses speed only so fast: a start, a stop or a burst is a ramp, not a jump.
        double length = v.length();
        double previous = this.velocity.length();
        double wanted = Mth.clamp(length, minSpeed, maxSpeed);
        wanted = Math.min(wanted, previous + s.acceleration() * own * this.quickness);
        wanted = Math.max(wanted, previous - s.deceleration() * own * this.quickness);
        v = length < 1.0E-5 ? direction(this.headingYaw, this.headingPitch).scale(wanted) : v.scale(wanted / length);
        this.velocity = v;
        ((SteeredMob) this.mob).lifeguard$steer(v);
        this.face(v);
    }

    /**
     * Keeps it under the surface and off the floor, and turns it toward open water when a wall is ahead: a wall comes
     * before the school. A schooling fish's turn away is a decision its schoolmates take up, so the school turns
     * together (not while it hangs in a pause). Boxed in on every side, it turns right around once, not every tick.
     */
    private Vec3 avoidWalls(Vec3 pos, Vec3 v, BoidSpecies s, long now, boolean holding) {
        Level level = this.mob.level();
        AABB box = this.mob.getBoundingBox();
        double up = v.y;
        if (!open(level, BlockPos.containing(pos.x, box.maxY + 0.4, pos.z)))
            up = Math.min(up, 0.0) - 0.01;
        if (!open(level, BlockPos.containing(pos.x, box.minY - 0.4, pos.z)))
            up = Math.max(up, 0.0) + 0.006;
        v = new Vec3(v.x, up, v.z);
        if (this.corneredFor > 0)
            this.corneredFor--;

        double speed = v.length();
        if (speed < 1.0E-5)
            return v;
        double look = s.wallLookahead() + this.mob.getBbWidth() * 0.5;
        if (clear(level, pos, v.scale(1.0 / speed), look))
            return v;
        Vec3 flat = v.horizontalDistanceSqr() > 1.0E-6 ? new Vec3(v.x, 0.0, v.z).normalize() : direction(this.headingYaw, 0.0F);
        for (int turn : TURNS) {
            Vec3 out = flat.yRot(turn * Mth.DEG_TO_RAD);
            if (clear(level, pos, out, look)) {
                this.headingYaw = yawOf(out);
                if (this.mood == Mood.SCHOOLING && !holding && now - this.decidedAt() > 20)
                    this.decide(Kind.TURN, now, this.headingYaw, 0.0F, this.pace);
                return v.add(out.scale(speed)
                    .subtract(v)
                    .scale(0.25));
            }
        }
        if (this.corneredFor > 0)
            return v;
        this.corneredFor = 10;
        Vec3 back = new Vec3(-v.x, v.y, -v.z);
        this.headingYaw = yawOf(back);
        return back;
    }

    private static boolean clear(Level level, Vec3 pos, Vec3 direction, double look) {
        return open(level, BlockPos.containing(pos.add(direction.scale(look * 0.5)))) && open(level, BlockPos.containing(pos.add(direction.scale(look))));
    }

    /** Water a fish can swim through: loaded (never loads a chunk), water, and nothing solid in it (kelp is fine). */
    private static boolean open(Level level, BlockPos pos) {
        if (!level.isLoaded(pos))
            return false;
        BlockState state = level.getBlockState(pos);
        return state.getFluidState()
            .is(FluidTags.WATER) && state.getCollisionShape(level, pos)
                .isEmpty();
    }

    /** Points it the way it swims, turning a little each tick rather than snapping. */
    private void face(Vec3 v) {
        if (v.horizontalDistanceSqr() > 1.0E-6) {
            float yaw = Mth.approachDegrees(this.mob.getYRot(), yawOf(v) * Mth.RAD_TO_DEG, 20.0F);
            this.mob.setYRot(yaw);
            this.mob.yBodyRot = yaw;
            this.mob.yHeadRot = yaw;
        }
        float pitch = (float) -(Mth.atan2(v.y, v.horizontalDistance()) * Mth.RAD_TO_DEG);
        this.mob.setXRot(Mth.approachDegrees(this.mob.getXRot(), pitch, 10.0F));
    }

    /**
     * The nearest of its kind (and variant, if asked) other than itself, at most {@code limit}: one pass keeping the best
     * few, the variant read only for a fish close enough to make the cut.
     */
    private List<Mob> nearest(Vec3 pos, double radius, int limit, boolean sameVariant) {
        Object variant = sameVariant ? BoidVariants.of(this.mob) : null;
        List<? extends Mob> found = this.mob.level()
            .getEntitiesOfClass(this.mob.getClass(), new AABB(pos, pos).inflate(radius),
                other -> other != this.mob && other.getType() == this.type && other.isAlive());
        int k = Math.min(limit, found.size());
        Mob[] best = new Mob[k];
        double[] far = new double[k];
        int n = 0;
        for (Mob other : found) {
            double d = other.distanceToSqr(pos.x, pos.y, pos.z);
            if (n == k && d >= far[n - 1] || sameVariant && !Objects.equals(variant, BoidVariants.of(other)))
                continue;
            int i = n < k ? n++ : k - 1;
            while (i > 0 && far[i - 1] > d) {
                best[i] = best[i - 1];
                far[i] = far[i - 1];
                i--;
            }
            best[i] = other;
            far[i] = d;
        }
        List<Mob> nearest = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
            nearest.add(best[i]);
        return nearest;
    }

    // --- Little things

    /** True with this chance per second, rolled each tick. */
    private boolean chance(float perSecond) {
        return perSecond > 0.0F && this.random.nextFloat() < perSecond / 20.0F;
    }

    private int ticks(float minSeconds, float maxSeconds) {
        return Math.max(1, Math.round(20.0F * Mth.lerp(this.random.nextFloat(), minSeconds, maxSeconds)));
    }

    /** Minecraft's yaw, in radians, of a direction. */
    private static float yawOf(Vec3 v) {
        return (float) Mth.atan2(-v.x, v.z);
    }

    private static Vec3 direction(float yaw, float pitch) {
        float flat = Mth.cos(pitch);
        return new Vec3(-Mth.sin(yaw) * flat, Mth.sin(pitch), Mth.cos(yaw) * flat);
    }

    /** An angle (radians) moved toward another by at most {@code step}, the short way round. */
    private static float turnToward(float from, float to, float step) {
        float gap = Mth.wrapDegrees((to - from) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
        return from + Mth.clamp(gap, -step, step);
    }

    /** A velocity turned toward a heading (yaw and pitch, radians) by at most {@code step} each, its speed kept. */
    private static Vec3 rotateToward(Vec3 v, float yaw, float pitch, float step) {
        double speed = v.length();
        if (speed < 1.0E-5)
            return v;
        float vYaw = yawOf(v);
        float vPitch = (float) Mth.atan2(v.y, v.horizontalDistance());
        return direction(turnToward(vYaw, yaw, step), turnToward(vPitch, pitch, step)).scale(speed);
    }

}
