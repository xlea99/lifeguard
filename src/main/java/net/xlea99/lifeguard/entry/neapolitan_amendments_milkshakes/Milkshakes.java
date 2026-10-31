package net.xlea99.lifeguard.entry.neapolitan_amendments_milkshakes;

import java.util.Collections;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.xlea99.lifeguard.boot.BrokenEntries;
import org.jetbrains.annotations.Nullable;

/**
 * Neapolitan's ice cream turns a cauldron of milk into that flavor's milkshake cauldron, at the same level, when the milk
 * is in an Amendments cauldron too (where milk lives when Neapolitan's own milk cauldron is off). Done the way Neapolitan
 * does it on its milk cauldron: the ice cream is used up, its bowl comes back. Follows Neapolitan's Milkshake Cauldrons
 * setting, and reads Neapolitan only by name.
 */
public final class Milkshakes {
    private Milkshakes() {
    }

    /** The milkshake cauldron this ice cream makes, or null when it isn't one of Neapolitan's ice creams. */
    @Nullable
    public static Block milkshakeCauldronFor(ItemStack stack) {
        ResourceLocation item = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!item.getNamespace().equals("neapolitan") || !item.getPath().endsWith("_ice_cream"))
            return null;
        String flavor = item.getPath().substring(0, item.getPath().length() - "_ice_cream".length());
        Block cauldron = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("neapolitan", flavor + "_milkshake_cauldron"));
        return cauldron == Blocks.AIR || !(cauldron instanceof LayeredCauldronBlock) || !milkshakeCauldronsOn() ? null : cauldron;
    }

    /** {@code layers} of milk at {@code pos} become {@code cauldron} at that level, the ice cream traded for its bowl. */
    public static ItemInteractionResult makeMilkshake(Block cauldron, int layers, ItemStack stack, Level level, BlockPos pos,
                                                     Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            int top = Collections.max(LayeredCauldronBlock.LEVEL.getPossibleValues());
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BOWL)));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            level.setBlockAndUpdate(pos, cauldron.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, Math.min(top, layers)));
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_POWDER_SNOW, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean milkshakeCauldronsOn() {
        try {
            Class<?> config = Class.forName("com.teamabnormals.neapolitan.core.NeapolitanConfig");
            Object common = config.getField("COMMON").get(null);
            Object value = common.getClass().getField("milkshakeCauldrons").get(common);
            return !Boolean.FALSE.equals(value.getClass().getMethod("get").invoke(value));
        } catch (ReflectiveOperationException | RuntimeException e) {
            BrokenEntries.mark(NeapolitanAmendmentsMilkshakes.ENTRY, "Neapolitan's NeapolitanConfig.COMMON.milkshakeCauldrons"
                + " can't be read (" + e + "), so it can't tell whether milkshake cauldrons are on");
            return false;
        }
    }
}
