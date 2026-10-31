package net.xlea99.lifeguard.entry.jukebox_playlists;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.Lifeguard;
import org.jetbrains.annotations.Nullable;

/**
 * Jukebox playlists, after Minecraft 26.4's: every chest touching a jukebox (any block in {@link #CONTAINERS}, on any
 * side; a double chest is the whole chest) is its playlist, all of them one pool. 26.4 only takes a chest on top; here
 * the top is free, so a disc spinning on the jukebox (Amendments) can still be seen. When a song finishes, the jukebox
 * swaps its disc with the next disc in the pool, skipping empty slots and anything that isn't a disc, looping back to
 * the start. In order, the pool reads its chests top, north, east, south, west, bottom, each in its own slot order. With
 * no discs in the pool, the jukebox loops the disc it has.
 *
 * <p>Tracks change after a short silence, so the jukebox's redstone signal drops for a moment between songs (a "new
 * track" pulse); with {@code jukebox_progress_signal}, comparators go back to 1 as each track starts.
 *
 * <p>{@link JukeboxPlaylists#RADIO_CONTROLS}: a jukebox with a playlist is a radio. Right-clicking it while it plays skips
 * to the next disc (the playing one goes back where the next one was); sneak-right-clicking it with an empty hand turns
 * it off (the disc goes back into a chest); right-clicking it empty, with an empty hand, turns it on.
 *
 * <p>{@link JukeboxPlaylists#SHUFFLE}: a block from {@link #SHUFFLERS} touching the jukebox, on any side (a dropper or
 * dispenser, which already pick a random slot), plays the playlist shuffled: every song once, in a random order, before
 * any plays again, and never the same song twice running. Etcetera's dice, as a shuffler, rolls with each pick.
 */
public final class Playlists {
    /** Blocks that work as a jukebox's playlist, touching it. */
    public static final TagKey<Block> CONTAINERS =
        TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "jukebox_containers"));
    /** Blocks that make the jukebox they touch shuffle its playlist. */
    public static final TagKey<Block> SHUFFLERS =
        TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "make_jukebox_shuffle"));

    /** Ticks of silence between a finished song and the next, on top of vanilla's second after the song. */
    public static final int GAP_TICKS = 10;

    /** The order a playlist reads its chests in. */
    private static final Direction[] ORDER = {Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH,
        Direction.WEST, Direction.DOWN};

    private static final ResourceLocation DICE = ResourceLocation.fromNamespaceAndPath("etcetera", "dice");

    /** One slot of a playlist: a chest, and a slot in it. */
    private record Slot(Container chest, int slot) {
        ItemStack item() {
            return this.chest.getItem(this.slot);
        }
    }

    private Playlists() {
    }

    /** The song in {@code jukebox} played to its end (not taken out): the next track comes after a short silence. */
    public static void songEnded(JukeboxBlockEntity jukebox) {
        Level level = jukebox.getLevel();
        if (level != null && !level.isClientSide && JukeboxPlaylists.ENTRY.active()
            && !chests(level, jukebox.getBlockPos()).isEmpty())
            ((PlaylistJukebox) jukebox).lifeguard$waitForNextTrack(GAP_TICKS);
    }

    /**
     * A right-click on a jukebox with a playlist touching it: the radio's buttons. Null leaves the click to vanilla (no
     * playlist, or a disc in the off hand going into an empty jukebox, or anything held while it's empty: placing a
     * block against it).
     */
    @Nullable
    public static InteractionResult use(BlockState state, Level level, BlockPos pos, Player player) {
        if (!JukeboxPlaylists.RADIO_CONTROLS.active() || chests(level, pos).isEmpty())
            return null;
        boolean hasDisc = state.getValue(JukeboxBlock.HAS_RECORD);
        if (!hasDisc && (!player.getMainHandItem().isEmpty() || isDisc(player.getOffhandItem())))
            return null;
        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox))
            return null;
        if (!hasDisc)
            return nextTrack(jukebox) ? InteractionResult.CONSUME : InteractionResult.PASS;
        if (player.isSecondaryUseActive())
            turnOff(jukebox, player);
        else
            nextTrack(jukebox);
        return InteractionResult.CONSUME;
    }

    /** The radio off: the playing disc goes back into a chest (or to the player, if they're full), and it's quiet. */
    private static void turnOff(JukeboxBlockEntity jukebox, Player player) {
        List<Slot> pool = pool(chests(jukebox.getLevel(), jukebox.getBlockPos()));
        ((PlaylistJukebox) jukebox).lifeguard$cancelNextTrack();
        ItemStack disc = jukebox.splitTheItem(1);
        if (disc.isEmpty() || pool.isEmpty())
            return;
        for (Slot slot : pool) {
            if (slot.item().isEmpty()) {
                slot.chest().setItem(slot.slot(), disc);
                slot.chest().setChanged();
                return;
            }
        }
        if (!player.addItem(disc))
            player.drop(disc, false);
    }

    /**
     * The next track: when a song finishes or is skipped, its disc is swapped into the chest slot the next one came
     * from; an empty jukebox just takes the next one. With no discs in the pool, the one the jukebox has plays again.
     * False if nothing plays.
     */
    public static boolean nextTrack(JukeboxBlockEntity jukebox) {
        Level level = jukebox.getLevel();
        if (!JukeboxPlaylists.ENTRY.active() || level == null || level.isClientSide)
            return false;
        BlockPos pos = jukebox.getBlockPos();
        // A jukebox being broken is already gone from its spot; a disc put in now would be lost with it.
        if (!(level.getBlockState(pos).getBlock() instanceof JukeboxBlock))
            return false;
        List<Container> chests = chests(level, pos);
        if (chests.isEmpty())
            return false;
        List<Slot> pool = pool(chests);
        PlaylistJukebox playlist = (PlaylistJukebox) jukebox;
        ItemStack current = jukebox.getTheItem();
        List<BlockPos> shufflers = JukeboxPlaylists.SHUFFLE.active() ? shufflers(level, pos) : List.of();
        Slot slot = shufflers.isEmpty() ? inOrder(pool, playlist)
            : shuffled(pool, playlist, level.random, current, level.registryAccess());
        if (slot == null) {
            // No discs in the pool: loop the one the jukebox has.
            Optional<Holder<JukeboxSong>> song = JukeboxSong.fromStack(level.registryAccess(), current);
            song.ifPresent(holder -> jukebox.getSongPlayer().play(level, holder));
            return song.isPresent();
        }
        ItemStack next = slot.item().split(1);
        if (!current.isEmpty()) {
            if (slot.item().isEmpty())
                slot.chest().setItem(slot.slot(), current.copy());
            else
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5, current.copy());
        }
        slot.chest().setChanged();
        jukebox.setTheItem(next);
        for (BlockPos shuffler : shufflers) {
            Block block = level.getBlockState(shuffler).getBlock();
            if (DICE.equals(BuiltInRegistries.BLOCK.getKey(block)))
                // A dice rolls on its own scheduled tick: a random face, its sound, a new comparator reading.
                level.scheduleTick(shuffler, block, 1);
        }
        return true;
    }

    /** The shufflers touching the jukebox, on any side. */
    private static List<BlockPos> shufflers(Level level, BlockPos jukebox) {
        List<BlockPos> found = new ArrayList<>();
        for (Direction side : ORDER)
            if (level.getBlockState(jukebox.relative(side)).is(SHUFFLERS))
                found.add(jukebox.relative(side));
        return found;
    }

    /**
     * The playlist's chests touching the jukebox, in {@link #ORDER}. A double chest is the whole chest, through the half
     * that touches (both halves can't: the blocks around a jukebox only meet each other diagonally).
     */
    private static List<Container> chests(Level level, BlockPos jukebox) {
        List<Container> chests = new ArrayList<>();
        for (Direction side : ORDER) {
            BlockPos pos = jukebox.relative(side);
            BlockState state = level.getBlockState(pos);
            if (!state.is(CONTAINERS))
                continue;
            Container chest = state.getBlock() instanceof ChestBlock block ? ChestBlock.getContainer(block, state, level, pos, true)
                : level.getBlockEntity(pos) instanceof Container container ? container : null;
            if (chest != null)
                chests.add(chest);
        }
        return chests;
    }

    /** Every slot of the playlist, chest after chest. */
    private static List<Slot> pool(List<Container> chests) {
        List<Slot> pool = new ArrayList<>();
        for (Container chest : chests)
            for (int slot = 0; slot < chest.getContainerSize(); slot++)
                pool.add(new Slot(chest, slot));
        return pool;
    }

    /** Anything a jukebox takes (vanilla's own test): modded discs and Caverns & Chasms' copies too. */
    private static boolean isDisc(ItemStack stack) {
        return stack.has(DataComponents.JUKEBOX_PLAYABLE);
    }

    /** The song a disc plays, by id (so two copies of different discs differ), or the item's id. */
    private static String song(ItemStack stack, HolderLookup.Provider registries) {
        return JukeboxSong.fromStack(registries, stack).flatMap(Holder::unwrapKey).map(key -> key.location().toString())
            .orElseGet(() -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }

    /** The first disc from the cursor on, wrapping around; null if there's none. */
    @Nullable
    private static Slot inOrder(List<Slot> pool, PlaylistJukebox playlist) {
        int size = pool.size();
        for (int i = 0; i < size; i++) {
            int index = Math.floorMod(playlist.lifeguard$cursor() + i, size);
            if (isDisc(pool.get(index).item())) {
                playlist.lifeguard$setCursor(index + 1);
                return pool.get(index);
            }
        }
        return null;
    }

    /** A random song not played yet this round (never the one just playing, if there's another); null if there's none. */
    @Nullable
    private static Slot shuffled(List<Slot> pool, PlaylistJukebox playlist, RandomSource random, ItemStack current,
                                 HolderLookup.Provider registries) {
        List<Slot> discs = pool.stream().filter(slot -> isDisc(slot.item())).toList();
        if (discs.isEmpty())
            return null;
        List<String> played = playlist.lifeguard$played();
        String playing = current.isEmpty() ? null : song(current, registries);
        if (playing != null && !played.contains(playing))
            played.add(playing);
        List<Slot> fresh = discs.stream().filter(slot -> !played.contains(song(slot.item(), registries))).toList();
        if (fresh.isEmpty()) {
            // A new round: everything's fresh again, except the song that just played.
            played.clear();
            fresh = discs.stream().filter(slot -> !song(slot.item(), registries).equals(playing)).toList();
            if (fresh.isEmpty())
                fresh = discs;
        }
        Slot slot = fresh.get(random.nextInt(fresh.size()));
        played.add(song(slot.item(), registries));
        return slot;
    }
}
