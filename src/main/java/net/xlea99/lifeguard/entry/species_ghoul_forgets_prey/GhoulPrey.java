package net.xlea99.lifeguard.entry.species_ghoul_forgets_prey;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Species' Ghouls let go of prey they can't have ({@link SpeciesGhoulForgetsPrey}, #17).
 *
 * <p>A Ghoul's search sets its target straight on the mob ({@code Ghoul.SearchGoal.aggro}), not through a target goal,
 * and only a target goal ever lets a target go (or the Ghoul's own stun). So prey that died without hitting back (a cow,
 * a fish, a spider someone else killed, the player who respawned) stayed its target for good: it never searched again,
 * and wouldn't notice anyone moving beside it until it was hurt. Prey it couldn't reach (a bat on the ceiling) held it
 * the same way, panting. Dead prey is now let go at once, and prey it hasn't landed a hit on in {@link #GRACE_TICKS} is
 * let go too; then it searches again as usual. A target the Ghoul's revenge goal still wants (it was hurt, and can still
 * see who did it) comes straight back, as before. Species isn't compiled against: the Ghoul is found by its id.
 */
public final class GhoulPrey {
    private static final ResourceLocation GHOUL = ResourceLocation.fromNamespaceAndPath("species", "ghoul");
    /** 20 seconds without landing a hit. */
    private static final int GRACE_TICKS = 400;

    /** Per Ghoul hunting something: how long it's gone without landing a hit, and the last hit it saw land. */
    private static final Map<Mob, Hunt> HUNTS = new WeakHashMap<>();

    private static boolean looked;
    private static EntityType<?> ghoulType;

    private GhoulPrey() {
    }

    private static final class Hunt {
        LivingEntity prey;
        int ticksSinceHit;
        int lastHitStamp;
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EntityTickEvent.Post.class, GhoulPrey::ticked);
    }

    private static boolean isGhoul(LivingEntity entity) {
        if (!looked) {
            looked = true;
            ghoulType = BuiltInRegistries.ENTITY_TYPE.getOptional(GHOUL).orElse(null);
        }
        return ghoulType != null && entity.getType() == ghoulType;
    }

    private static void ticked(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob ghoul) || ghoul.level().isClientSide() || !isGhoul(ghoul))
            return;
        LivingEntity prey = ghoul.getTarget();
        if (prey == null) {
            HUNTS.remove(ghoul);
            return;
        }
        if (!prey.isAlive()) {
            forget(ghoul);
            return;
        }
        Hunt hunt = HUNTS.computeIfAbsent(ghoul, g -> new Hunt());
        if (hunt.prey != prey) {
            hunt.prey = prey;
            hunt.ticksSinceHit = 0;
            hunt.lastHitStamp = prey.getLastHurtByMobTimestamp();
        }
        if (prey.getLastHurtByMob() == ghoul && prey.getLastHurtByMobTimestamp() != hunt.lastHitStamp) {
            hunt.lastHitStamp = prey.getLastHurtByMobTimestamp();
            hunt.ticksSinceHit = 0;
        } else if (++hunt.ticksSinceHit >= GRACE_TICKS)
            forget(ghoul);
    }

    private static void forget(Mob ghoul) {
        ghoul.setTarget(null);
        HUNTS.remove(ghoul);
    }
}
