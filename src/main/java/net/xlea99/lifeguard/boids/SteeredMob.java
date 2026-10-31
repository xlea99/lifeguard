package net.xlea99.lifeguard.boids;

import net.minecraft.world.phys.Vec3;

/**
 * A mob a boids goal steers ({@link BoidSchoolingGoal}): the velocity it sets is applied at the end of the mob's AI step,
 * after the mob's own movement code has run (a vanilla fish's move control lifts it a little every tick in water, which
 * would otherwise skew every school upward).
 */
public interface SteeredMob {

    void lifeguard$steer(Vec3 velocity);

}
