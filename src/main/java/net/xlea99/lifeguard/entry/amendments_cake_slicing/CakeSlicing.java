package net.xlea99.lifeguard.entry.amendments_cake_slicing;

import javax.annotation.Nullable;
import net.mehvahdjukaar.amendments.common.block.DirectionalCakeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * A knife slices Amendments' cakes as it would a plain cake: a stacked cake's top cake, and a cake eaten from a side
 * (which Amendments turns into its own directional cake).
 *
 * <p>Knives slice cakes through each mod's own right-click handling (Farmer's Delight for vanilla cake, Abnormals
 * Delight for Neapolitan's, ...), and each knows only its own plain cake. Amendments' cakes are its own blocks, so none
 * of them knew them, and the click went on to the cake, which eats a slice whenever the player can eat (Amendments
 * 1.20.1 sliced its directional cake for Farmer's Delight itself; 1.21 never calls that). So for a knife's right-click
 * on one, the plain cake (bitten as far as it is) stands in for it while every mod's handler runs. If one took a slice,
 * Amendments' cake comes back with a slice off by Amendments' own bite (it faces the player; a stack becomes a single
 * cake once the top is gone); if none did, it comes back as it was, and Amendments carries on as before. It's the trick
 * Amendments' stacked cake uses for its own right-click, earlier in the click. The stand-in is placed and taken back
 * without telling clients or neighbors, so nobody sees it unless a handler changes it.
 */
public final class CakeSlicing {
    /** Knives as Farmer's Delight tells them (its tag or the knife ability), and the common tag. */
    private static final TagKey<Item> COMMON_KNIVES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/knife"));
    private static final TagKey<Item> FD_KNIVES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("farmersdelight", "tools/knives"));
    private static final ItemAbility KNIFE_HARVEST = ItemAbility.get("knife_harvest");
    private static final int QUIET = Block.UPDATE_INVISIBLE | Block.UPDATE_KNOWN_SHAPE;

    /** The click in progress: Amendments' cake taken out of the world, and the plain cake standing in for it. */
    private record StandIn(Level level, BlockPos pos, BlockState stacked, BlockState top) {
    }

    @Nullable
    private static StandIn standIn;

    private CakeSlicing() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, PlayerInteractEvent.RightClickBlock.class,
            CakeSlicing::onRightClickFirst);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, PlayerInteractEvent.RightClickBlock.class,
            CakeSlicing::onRightClickLast);
        NeoForge.EVENT_BUS.addListener(CakeSlicing::onServerTick);
    }

    private static boolean isKnife(ItemStack stack) {
        return stack.is(COMMON_KNIVES) || stack.is(FD_KNIVES) || stack.canPerformAction(KNIFE_HARVEST);
    }

    /**
     * On the server, puts the stand-in in. On the client, stops the click there: left to itself, the client guesses the
     * click's outcome by running the stacked cake's own right-click, which eats a slice, and its hunger bar kept that
     * bite (the server only re-sends hunger when its own changes, and the server didn't eat). The click still goes to
     * the server, which decides.
     */
    private static void onRightClickFirst(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (!isKnife(event.getItemStack()))
            return;
        if (!level.isClientSide())
            putBack();
        BlockPos pos = event.getPos();
        BlockState stacked = level.getBlockState(pos);
        if (!(stacked.getBlock() instanceof DirectionalCakeBlock cake))
            return;
        if (level.isClientSide()) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }
        BlockState top = cake.type.cake.withPropertiesOf(stacked);
        level.setBlock(pos, top, QUIET);
        standIn = new StandIn(level, pos.immutable(), stacked, top);
    }

    private static void onRightClickLast(PlayerInteractEvent.RightClickBlock event) {
        StandIn click = standIn;
        if (click == null || click.level() != event.getLevel() || !click.pos().equals(event.getPos()))
            return;
        standIn = null;
        if (click.level().getBlockState(click.pos()).equals(click.top())) {
            click.level().setBlock(click.pos(), click.stacked(), QUIET);
            return;
        }
        ((DirectionalCakeBlock) click.stacked().getBlock()).removeSlice(click.stacked(), click.pos(), click.level(),
            event.getEntity(), DirectionalCakeBlock.getHitDir(event.getEntity(), event.getHitVec()));
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    /** A handler that threw ended the click before {@link #onRightClickLast}: put Amendments' cake back as it was. */
    private static void onServerTick(ServerTickEvent.Post event) {
        putBack();
    }

    private static void putBack() {
        StandIn click = standIn;
        if (click == null)
            return;
        standIn = null;
        click.level().setBlock(click.pos(), click.stacked(), Block.UPDATE_ALL);
    }
}
