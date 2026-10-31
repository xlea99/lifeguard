package net.xlea99.lifeguard.duct;

import com.teamabnormals.caverns_and_chasms.common.block.StorageDuctBlock;
import com.teamabnormals.caverns_and_chasms.core.registry.CCSoundEvents;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.xlea99.lifeguard.entry.cc_storage_duct_item_loss.CcStorageDuctItemLoss;
import net.xlea99.lifeguard.entry.cc_storage_duct_span_limit.CcStorageDuctSpanLimit;

/**
 * Caverns & Chasms' Storage Ducts: a run of connected ducts is one inventory, opened from either end or a hatch, and
 * every duct hands that whole inventory to hoppers and pipes. The {@code shared.cc_storage_ducts} mixins.
 *
 * <p>{@link CcStorageDuctItemLoss}: each duct kept the network it built once and rebuilt it only when that one duct
 * changed. A far duct's chunk unloading discards its block entity without telling the others, so a hopper kept pouring
 * items into the discarded one, gone when the chunk loaded again; and every insert called {@code setChanged} on each
 * discarded duct, whose comparator update loaded its chunk back in (ISSUES #35, proven on 3.0.1). Here a duct hands
 * out a {@link DuctNetworkHandler} that rebuilds its network whenever any duct anywhere was placed, broken, reshaped,
 * loaded or unloaded since (one counter, so checking is one comparison).
 *
 * <p>{@link CcStorageDuctSpanLimit}: finding a network walks every duct in it, loading chunks as it goes, however far
 * the run reaches. A network now has to fit in a box of {@code max_span} chunks either way. One that doesn't is sealed,
 * all of it, nothing in or out, which depends only on its shape, so it's never half there. Ducts still place freely;
 * placing the one that seals a network says so, and so does trying to open one. The check stops as soon as a run leaves
 * the box, so it never loads more than the box and one chunk past.
 *
 * <p>Either entry takes the networks over from Caverns & Chasms' own keeping, so the span limit alone loses nothing
 * either.
 */
public final class StorageDucts {
    /** Moves whenever any duct is placed, broken, reshaped, loaded or unloaded; networks built before it are rebuilt. */
    private static final AtomicInteger GENERATION = new AtomicInteger();

    private StorageDucts() {
    }

    public static boolean managed() {
        return CcStorageDuctItemLoss.ENTRY.active() || spanLimit();
    }

    private static boolean spanLimit() {
        return CcStorageDuctSpanLimit.ENTRY.active();
    }

    /** The generation and the span setting together: a network built under another stamp is rebuilt. */
    static long stamp() {
        return ((long) GENERATION.get() << 8) | (spanLimit() ? CcStorageDuctSpanLimit.MAX_SPAN.get() : 0);
    }

    public static void changed() {
        GENERATION.incrementAndGet();
    }

    /** Whether the network through the duct at {@code pos} is sealed for being too wide. */
    public static boolean sealed(Level level, BlockPos pos, BlockState state) {
        return spanLimit() && !fits(level, pos, state, CcStorageDuctSpanLimit.MAX_SPAN.get());
    }

    public static void tellSealed(ServerPlayer player, Level level, BlockPos pos, String message) {
        player.displayClientMessage(Component.translatable(message, CcStorageDuctSpanLimit.MAX_SPAN.get()), true);
        level.playSound(null, pos, CCSoundEvents.STORAGE_DUCT_HATCH_CLOSE.get(), SoundSource.BLOCKS, 1.0F, 0.6F);
    }

    /** Tells whoever just made a network too wide. Registered when the span limit is on. */
    public static void registerPlaceWarning() {
        NeoForge.EVENT_BUS.addListener(StorageDucts::placed);
    }

    private static void placed(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide() && event.getEntity() instanceof ServerPlayer player
            && event.getPlacedBlock().getBlock() instanceof StorageDuctBlock && sealed(level, event.getPos(), event.getPlacedBlock()))
            tellSealed(player, level, event.getPos(), "lifeguard.message.storage_duct_too_wide");
    }

    /**
     * Walks the run both ways from {@code start} the way Caverns & Chasms does (a duct joins the next one when one of
     * the next one's ends points back), keeping the box of chunks it covers. False as soon as a duct would make the box
     * wider than {@code max} chunks either way; a loop back to {@code start} is the whole run.
     */
    private static boolean fits(Level level, BlockPos start, BlockState startState, int max) {
        if (!(startState.getBlock() instanceof StorageDuctBlock))
            return true;
        int minX = start.getX() >> 4, maxX = minX, minZ = start.getZ() >> 4, maxZ = minZ;
        for (StorageDuctBlock.DuctEnd first : StorageDuctBlock.DuctEnd.values()) {
            BlockPos.MutableBlockPos at = start.mutable();
            BlockState state = startState;
            StorageDuctBlock.DuctEnd end = first;
            while (true) {
                Direction towards = state.getValue(end.getDirectionProperty());
                at.move(towards);
                state = level.getBlockState(at);
                StorageDuctBlock.DuctEnd back = endFacing(state, towards.getOpposite());
                if (back == null)
                    break;
                if (at.equals(start))
                    return true;
                int cx = at.getX() >> 4, cz = at.getZ() >> 4;
                minX = Math.min(minX, cx);
                maxX = Math.max(maxX, cx);
                minZ = Math.min(minZ, cz);
                maxZ = Math.max(maxZ, cz);
                if (maxX - minX + 1 > max || maxZ - minZ + 1 > max)
                    return false;
                end = back.getOpposite();
            }
        }
        return true;
    }

    private static StorageDuctBlock.DuctEnd endFacing(BlockState state, Direction facing) {
        if (!(state.getBlock() instanceof StorageDuctBlock))
            return null;
        for (StorageDuctBlock.DuctEnd end : StorageDuctBlock.DuctEnd.values())
            if (state.getValue(end.getDirectionProperty()) == facing)
                return end;
        return null;
    }
}
