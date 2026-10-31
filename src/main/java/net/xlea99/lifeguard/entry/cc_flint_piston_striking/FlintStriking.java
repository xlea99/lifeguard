package net.xlea99.lifeguard.entry.cc_flint_piston_striking;

import com.teamabnormals.caverns_and_chasms.common.block.FlintBlock;
import com.teamabnormals.caverns_and_chasms.core.registry.CCBlocks;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Caverns & Chasms' flint block sparks when a piston rubs it against iron ({@link CcFlintPistonStriking}).
 *
 * <p>Supplementaries' block of flint strikes fire when a piston slides it along iron, or slides iron along it. Caverns
 * & Chasms' flint block only sparks when it lands from a fall (or, since 3.0, is brushed or grazed by a falling block).
 * This gives it Supplementaries' striking, with Caverns & Chasms' own full spark (its particles, sound, lit flash and
 * fire), so a pack can keep the one flint block and lose neither. A rub is a slide past: the flint and the striker side
 * by side, one moving along the other, before or after the move; pushing one head-on into the other does nothing, as in
 * Supplementaries. Strikers are Supplementaries' own {@code #supplementaries:flint_metals}.
 *
 * <p>The piston's move is read before it happens, and the spark waits until the moving block has arrived, so the flash
 * shows on the flint where it ends up, the tick it's set down. A flint block left hanging by the push then falls as
 * usual, and sparks again where it lands.
 */
public final class FlintStriking {
    private static final TagKey<Block> STRIKERS = TagKey.create(Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath("supplementaries", "flint_metals"));
    /** How long to wait on a block that's still moving (a piston that keeps getting interrupted) before giving up. */
    private static final int GIVE_UP_TICKS = 20;

    private static final Map<Level, List<PendingSpark>> PENDING = new WeakHashMap<>();

    private FlintStriking() {
    }

    private record PendingSpark(BlockPos pos, long until) {
    }

    public static void register() {
        // Last, so a move another mod cancels never gets here.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, PistonEvent.Pre.class, FlintStriking::pistonMoving);
        NeoForge.EVENT_BUS.addListener(LevelTickEvent.Post.class, FlintStriking::levelTicked);
    }

    private static boolean isFlint(BlockState state) {
        return state.is(CCBlocks.FLINT_BLOCK.get());
    }

    private static void pistonMoving(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !CcFlintPistonStriking.ENTRY.active())
            return;
        // A plain piston fires a retract event too, but pulls nothing back.
        if (event.getPistonMoveType() == PistonEvent.PistonMoveType.RETRACT && !level.getBlockState(event.getPos()).is(Blocks.STICKY_PISTON))
            return;
        PistonStructureResolver structure = event.getStructureHelper();
        if (structure == null || !structure.resolve())
            return;
        Direction motion = structure.getPushDirection();
        Set<BlockPos> moving = new HashSet<>(structure.getToPush());
        moving.addAll(structure.getToDestroy());

        Set<BlockPos> sparks = new HashSet<>();
        for (BlockPos from : structure.getToPush()) {
            BlockState moved = level.getBlockState(from);
            boolean flint = isFlint(moved);
            boolean striker = moved.is(STRIKERS);
            if (!flint && !striker)
                continue;
            BlockPos to = from.relative(motion);
            for (Direction side : Direction.values()) {
                if (side.getAxis() == motion.getAxis())
                    continue;
                for (BlockPos beside : List.of(from.relative(side), to.relative(side))) {
                    // Something moving along with it doesn't rub.
                    if (moving.contains(beside))
                        continue;
                    BlockState other = level.getBlockState(beside);
                    if (flint && other.is(STRIKERS))
                        sparks.add(to);
                    else if (striker && isFlint(other))
                        sparks.add(beside.immutable());
                }
            }
        }
        if (sparks.isEmpty())
            return;
        long now = level.getGameTime();
        List<PendingSpark> pending = PENDING.computeIfAbsent(level, l -> new ArrayList<>());
        for (BlockPos pos : sparks)
            pending.add(new PendingSpark(pos.immutable(), now + GIVE_UP_TICKS));
    }

    private static void levelTicked(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level))
            return;
        List<PendingSpark> pending = PENDING.get(level);
        if (pending == null || pending.isEmpty())
            return;
        long now = level.getGameTime();
        // Sparked after the walk, so nothing it sets off can touch the list mid-walk.
        List<BlockPos> due = new ArrayList<>();
        for (Iterator<PendingSpark> it = pending.iterator(); it.hasNext();) {
            PendingSpark spark = it.next();
            BlockState state = level.getBlockState(spark.pos());
            if (isFlint(state)) {
                due.add(spark.pos());
                it.remove();
            } else if (!state.is(Blocks.MOVING_PISTON) || now >= spark.until())
                it.remove();
        }
        for (BlockPos pos : due)
            FlintBlock.spark(level, pos, false);
    }
}
