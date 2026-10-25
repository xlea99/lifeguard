package net.xlea99.lifeguard.entry.cc_golden_bucket_compat;

import com.mojang.datafixers.util.Pair;
import com.teamabnormals.caverns_and_chasms.common.item.GoldenBucketItem;
import com.teamabnormals.caverns_and_chasms.common.item.GoldenMilkBucketItem;
import com.teamabnormals.caverns_and_chasms.common.item.GoldenSolidBucketItem;
import com.teamabnormals.caverns_and_chasms.core.registry.CCItems;
import java.util.List;
import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.SoundActions;
import org.jetbrains.annotations.Nullable;

/**
 * Caverns & Chasms' golden buckets with Moonlight's tanks: Supplementaries' jars, Amendments' cauldrons, anything else
 * built on them.
 *
 * <p>A golden bucket carries up to three buckets of one liquid. Caverns & Chasms only teaches it vanilla's cauldrons
 * (water, lava, powder snow), and Moonlight lists a couple of golden buckets as plain one-bucket containers, so
 * emptying one into a jar handed back a plain bucket. Here a golden bucket pours one bucket in or scoops one out of any
 * tank, for any liquid a golden bucket exists for: which golden bucket goes with which liquid comes from the golden
 * bucket items themselves (each knows its fluid; milk and powder snow aren't fluids, so those two are matched by their
 * plain buckets). One bucket is whatever Moonlight counts the plain bucket as.
 */
public final class GoldenBuckets {
    @Nullable
    private static List<Item> goldenBuckets;

    private GoldenBuckets() {
    }

    /** A golden bucket, empty or of a liquid. Milk and powder snow ones are their own kinds of item. */
    public static boolean isGoldenBucket(Item item) {
        return item instanceof GoldenBucketItem || item instanceof GoldenMilkBucketItem || item instanceof GoldenSolidBucketItem;
    }

    /**
     * A golden bucket used on a tank: scoops a bucket out (an empty golden bucket, or one of the same liquid with room)
     * or pours its bucket in. False when it isn't a golden bucket or neither applies; true when handled, including a
     * full bucket that doesn't fit, which does nothing rather than pouring out beside the block.
     */
    public static boolean useOnTank(SoftFluidTank tank, Player player, InteractionHand hand, Level level, BlockPos pos) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isGoldenBucket(stack.getItem()))
            return false;
        HolderLookup.Provider registries = level.registryAccess();
        // A tank holding at least a bucket is scooped by an empty golden bucket, or by one of its liquid with room left,
        // as Caverns & Chasms does on a full vanilla cauldron.
        SoftFluidStack inside = tank.getFluid();
        Item golden = inside.isEmpty() ? null : goldenBucketFor(inside, registries);
        SoftFluidStack bucket = golden == null ? null : liquidOf(golden, registries);
        boolean empty = GoldenBucketItem.isEmpty(stack);
        if (bucket != null && inside.getCount() >= bucket.getCount()
            && (empty || stack.is(golden) && GoldenBucketItem.canBeFilled(stack))) {
            if (!level.isClientSide) {
                tank.removeFluid(bucket.getCount(), false);
                player.setItemInHand(hand, empty ? GoldenBucketItem.createFilledResult(stack, player, new ItemStack(golden))
                    : GoldenBucketItem.increaseFluidLevel(stack));
                play(level, pos, golden, true);
                level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
            }
            return true;
        }
        SoftFluidStack held = liquidOf(stack.getItem(), registries);
        if (held == null)
            return false;
        if (tank.addFluid(held.copy(), true) < held.getCount())
            return true;
        if (!level.isClientSide) {
            tank.addFluid(held.copy(), false);
            player.setItemInHand(hand, GoldenBucketItem.getEmptySuccessItem(stack, player));
            play(level, pos, stack.getItem(), false);
            level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
        }
        return true;
    }

    /** One bucket of the liquid in a filled golden bucket, or null (empty, or a liquid Moonlight doesn't know). */
    @Nullable
    public static SoftFluidStack liquidOf(Item item, HolderLookup.Provider registries) {
        ItemStack plain = plainBucketOf(item);
        if (plain.isEmpty())
            return null;
        Pair<SoftFluidStack, FluidContainerList.Category> liquid = SoftFluidStack.fromItem(plain, registries);
        if (liquid != null && !liquid.getFirst().isEmpty())
            return liquid.getFirst();
        if (!(item instanceof GoldenBucketItem golden) || golden.content == Fluids.EMPTY)
            return null;
        SoftFluidStack fluid = SoftFluidStack.fromFluid(golden.content, 4, registries);
        return fluid.isEmpty() ? null : fluid;
    }

    /** The plain bucket of what a filled golden bucket carries, or empty. */
    private static ItemStack plainBucketOf(Item item) {
        if (item == CCItems.GOLDEN_MILK_BUCKET.get())
            return new ItemStack(Items.MILK_BUCKET);
        if (item == CCItems.GOLDEN_POWDER_SNOW_BUCKET.get())
            return new ItemStack(Items.POWDER_SNOW_BUCKET);
        if (item instanceof GoldenBucketItem golden && golden.content != Fluids.EMPTY)
            return new ItemStack(golden.content.getBucket());
        return ItemStack.EMPTY;
    }

    /** The golden bucket that carries this liquid, or null. */
    @Nullable
    private static Item goldenBucketFor(SoftFluidStack liquid, HolderLookup.Provider registries) {
        if (goldenBuckets == null)
            goldenBuckets = BuiltInRegistries.ITEM.stream().filter(GoldenBuckets::isGoldenBucket).toList();
        for (Item item : goldenBuckets) {
            SoftFluidStack carried = liquidOf(item, registries);
            if (carried != null && liquid.is(carried.getHolder()))
                return item;
        }
        return null;
    }

    public static void play(Level level, BlockPos pos, Item golden, boolean fill) {
        if (golden == CCItems.GOLDEN_POWDER_SNOW_BUCKET.get()) {
            level.playSound(null, pos, fill ? SoundEvents.BUCKET_FILL_POWDER_SNOW : SoundEvents.BUCKET_EMPTY_POWDER_SNOW,
                SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }
        Fluid fluid = golden instanceof GoldenBucketItem bucket ? bucket.content : Fluids.EMPTY;
        SoundEvent sound = fluid == Fluids.EMPTY ? null : fluid.getFluidType().getSound(fill ? SoundActions.BUCKET_FILL : SoundActions.BUCKET_EMPTY);
        if (sound == null)
            sound = fill ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_EMPTY;
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
