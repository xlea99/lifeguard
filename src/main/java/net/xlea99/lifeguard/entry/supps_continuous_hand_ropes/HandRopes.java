package net.xlea99.lifeguard.entry.supps_continuous_hand_ropes;

import java.util.List;
import javax.annotation.Nullable;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.ContinuousPulleyMover;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.InstantPulleyMover;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PulleyStructureResolver;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A rope pulled up or let down by hand moves what hangs from it the way a pulley does (with Supplementaries'
 * continuous_retraction): animated, and with whatever is glued to it, up to the pulley's pull limit. The rope column is
 * handed to Supplementaries' pulley mover as if it hung from a pulley: raising, from the rope above the last one, which
 * takes the last rope in; lowering, from the last rope, which lets a new one out.
 *
 * <p>Like a pulley's, the move runs as a block event (on the last rope), so every client watching runs it too and draws
 * the blocks sliding: the server places moving blocks without telling clients.
 */
public final class HandRopes {
    /** The block event id on a rope; its param is 1 to lower, 0 to raise. */
    public static final int EVENT = 117;
    /** Ticks a move takes: a pulley turned by hand takes 2. */
    private static final int ANIMATION_TICKS = 2;

    public enum Result { NOT_MINE, BUSY, MOVED }

    private record Plan(Result result, @Nullable BlockPos lastRope, @Nullable PulleyStructureResolver resolver) {
        static final Plan NOT_MINE = new Plan(Result.NOT_MINE, null, null);
        static final Plan BUSY = new Plan(Result.BUSY, null, null);
        static final Plan PREDICTED = new Plan(Result.MOVED, null, null);
    }

    private HandRopes() {
    }

    /** A click on a rope ({@code start} is just below it): queues the move when it's this entry's. */
    public static Result click(Level level, BlockPos start, Block rope, boolean lowering) {
        Plan plan = plan(level, start, rope, lowering);
        if (plan.result() == Result.MOVED && !level.isClientSide)
            level.blockEvent(plan.lastRope(), level.getBlockState(plan.lastRope()).getBlock(), EVENT, lowering ? 1 : 0);
        return plan.result();
    }

    /** The queued move, run on the server and then on each client watching. */
    public static boolean onEvent(Level level, BlockPos lastRope, Block rope, int param) {
        Plan plan = plan(level, lastRope, rope, param == 1);
        if (plan.resolver() == null)
            return false;
        ContinuousPulleyMover.moveOneStep(level, plan.resolver(), ANIMATION_TICKS);
        return true;
    }

    private static Plan plan(Level level, BlockPos start, Block rope, boolean lowering) {
        if (!CommonConfigs.Redstone.PULLEY_CONTINUOUS.get())
            return Plan.NOT_MINE;
        BlockPos hanging = start;
        while (InstantPulleyMover.isCorrectRope(rope, level.getBlockState(hanging), Direction.DOWN))
            hanging = hanging.below();
        BlockPos lastRope = hanging.above();
        if (!InstantPulleyMover.isCorrectRope(rope, level.getBlockState(lastRope), Direction.DOWN))
            return Plan.NOT_MINE;
        BlockState hangingState = level.getBlockState(hanging);
        BlockPos moving = hangingState.getBlock() instanceof MovingPistonBlock ? hanging
            : hangingState.canBeReplaced() && level.getBlockState(hanging.below()).getBlock() instanceof MovingPistonBlock
            ? hanging.below() : null;
        if (moving != null) {
            // A client keeps a landed block moving until the server's update arrives, a tick or so after the server
            // has moved on: it predicts the move (swing, sound, rope) and leaves the world to the server's event.
            if (level.isClientSide && level.getBlockEntity(moving) instanceof PistonMovingBlockEntity m
                && m.getProgress(1.0F) >= 1.0F)
                return Plan.PREDICTED;
            // Still moving from the last click: wait for it, or a rope could be spent on a move that never lands.
            return Plan.BUSY;
        }
        // Nothing hangs (only rope moves) or a pulley carries on the column: Supplementaries' own handling.
        if (hangingState.canBeReplaced() || hangingState.getBlock() instanceof PulleyBlock)
            return Plan.NOT_MINE;
        PulleyStructureResolver.RopeColumn column = lowering
            ? new PulleyStructureResolver.RopeColumn(lastRope, rope, Direction.DOWN, true)
            : new PulleyStructureResolver.RopeColumn(lastRope.above(), rope, Direction.DOWN, false);
        PulleyStructureResolver resolver = new PulleyStructureResolver(level, List.of(column));
        // Too heavy or stuck: Supplementaries' instant move decides, as before.
        if (!resolver.resolve() || resolver.hasNothingToMove() || !resolver.getToPush().contains(hanging))
            return Plan.NOT_MINE;
        return new Plan(Result.MOVED, lastRope, resolver);
    }
}
