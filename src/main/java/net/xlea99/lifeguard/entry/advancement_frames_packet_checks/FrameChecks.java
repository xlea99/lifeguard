package net.xlea99.lifeguard.entry.advancement_frames_packet_checks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/**
 * What the server checks before a player fills an advancement or stat frame ({@link AdvancementFramesPacketChecks},
 * {@code mixin.advancement_frames_packet_checks}).
 *
 * <p>Advancement Frames trusted its two "set this frame" packets: any player could fill any frame (and become its owner)
 * from any distance, loading its chunk to do it, overwrite a filled one, and frame advancements they never earned. Its
 * client only ever sends them for an empty frame the player just clicked, holding a picker screen open, so the checks
 * are exactly that: the frame's chunk is loaded, the player can reach it, the frame is still empty and, for an
 * advancement, the player earned it.
 */
public final class FrameChecks {
    /** Vanilla gives block clicks one block of slack; a little more for the time spent in the picker. */
    private static final double REACH_SLACK = 2.0;

    private FrameChecks() {
    }

    /** The frame at {@code pos} is loaded and within the player's reach (checked before touching its block entity). */
    public static boolean canReach(ServerPlayer player, BlockPos pos) {
        return player.level().isLoaded(pos) && player.canInteractWithBlock(pos, REACH_SLACK);
    }
}
