package net.xlea99.lifeguard.entry.clayworks_flower_pots;

import com.teamabnormals.clayworks.common.DecoratedPotTrim;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hands a pot's color and design from a pot being removed to the pot replacing it.
 *
 * <p>Potting or un-potting a plant sets a different block, and the chunk destroys the old block entity and makes a
 * fresh one. Everything that swaps pots (vanilla's click, mods planting their own, pots that swap themselves) goes
 * through that path, and in it {@code onRemove} on the old block runs right before {@code newBlockEntity} for the new
 * one, on the same thread. So the old pot leaves its looks here and the new pot's constructor takes them. Thread-local
 * because worlds tick on more than one thread.
 */
public final class PendingPot {
    public record Looks(@Nullable DyeColor color, @Nullable DecoratedPotTrim design) {
    }

    private record Pending(BlockPos pos, Looks looks) {
    }

    private static final ThreadLocal<Pending> SLOT = new ThreadLocal<>();

    private PendingPot() {
    }

    public static void put(BlockPos pos, Looks looks) {
        SLOT.set(new Pending(pos.immutable(), looks));
    }

    /**
     * Whether the block replacing a pot will make a pot block entity that takes the looks. A mod pot with its own block
     * entity would leave them in the slot, and a plain pot placed there later could pick them up.
     */
    public static boolean willTake(BlockPos pos, BlockState newState) {
        return newState.getBlock() instanceof EntityBlock block && block.newBlockEntity(pos, newState) instanceof FlowerPotBlockEntity;
    }

    /** The looks left for this position, if any; always clears the slot. */
    @Nullable
    public static Looks take(BlockPos pos) {
        Pending pending = SLOT.get();
        if (pending == null)
            return null;
        SLOT.remove();
        return pending.pos().equals(pos) ? pending.looks() : null;
    }
}
