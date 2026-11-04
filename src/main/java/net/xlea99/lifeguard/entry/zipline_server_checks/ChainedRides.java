package net.xlea99.lifeguard.entry.zipline_server_checks;

import com.evandev.zipline.Cables;
import com.evandev.zipline.config.ModConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The server's own answer to "is this player really riding a zipline?". Zipline asks only whether the player is using
 * an attachment item; this adds what its client already checks before attaching, from the server's copy of the world:
 * a chain span within grabbing reach above where the player is (or claims to be moving to), using Zipline's own lookup
 * ({@link Cables#getClosestCable}, which reads Reconnectible Chains' knots), and a move no longer than a ride step.
 */
public final class ChainedRides {
    /**
     * Fastest a ride moves, in blocks per tick, before Zipline's `speedMultiplier`: the default mode eases toward 1.6;
     * `realisticPhysics` tops out at 0.04 * slope / (1 - 0.98) = 2 * slope, at most 2.
     */
    private static final double RIDE_SPEED = 2.0;
    /** Room for one move packet carrying slightly more than a tick (lag, rounding). */
    private static final double STEP_SLACK = 1.0;
    /** Room past Zipline's `snapRadius`: the client attaches within it, then the line sags and the rider swings. */
    private static final double REACH_SLACK = 1.0;

    private ChainedRides() {
    }

    /**
     * @param move where the move packet being handled claims the player is going, or null outside move handling (the
     *             flying check in the connection's tick): then the player's current position is judged.
     */
    public static boolean onAChain(Player rider, @Nullable Vec3 move) {
        ModConfig config = ModConfig.get();
        if (move != null && move.distanceTo(rider.position()) > RIDE_SPEED * config.speedMultiplier + STEP_SLACK)
            return false;
        Vec3 at = move != null ? move : rider.position();
        Vec3 grip = at.add(0.0, config.hangOffset, 0.0);
        return Cables.getClosestCable(rider.level(), grip, config.snapRadius + REACH_SLACK) != null;
    }
}
