package net.xlea99.lifeguard.entry.cauldron_fourths;

import java.util.Map;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.xlea99.lifeguard.Lifeguard;

/**
 * Vanilla's cauldron interactions that know a full cauldron is three levels: a water or powder snow bucket fills one to
 * 3, an empty bucket only takes from 3, and a water bottle won't pour into 3. They live as lambdas in vanilla's
 * interaction maps, and other mods reuse the bucket fills in their own maps (Neapolitan's milk cauldrons do), so once
 * every mod has added its interactions, each one that is still vanilla's is swapped for the same thing at four levels.
 * An interaction another mod has already replaced is left to that mod.
 */
public final class FourLevelInteractions {
    private static final int FULL = 4;

    private static final CauldronInteraction FILL_WATER = (state, level, pos, player, hand, stack) ->
        CauldronInteraction.emptyBucket(level, pos, player, hand, stack,
            Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, FULL), SoundEvents.BUCKET_EMPTY);
    private static final CauldronInteraction FILL_POWDER_SNOW = (state, level, pos, player, hand, stack) ->
        CauldronInteraction.emptyBucket(level, pos, player, hand, stack,
            Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, FULL),
            SoundEvents.BUCKET_EMPTY_POWDER_SNOW);
    private static final CauldronInteraction TAKE_WATER = (state, level, pos, player, hand, stack) ->
        CauldronInteraction.fillBucket(state, level, pos, player, hand, stack, new ItemStack(Items.WATER_BUCKET),
            full -> full.getValue(LayeredCauldronBlock.LEVEL) == FULL, SoundEvents.BUCKET_FILL);
    private static final CauldronInteraction TAKE_POWDER_SNOW = (state, level, pos, player, hand, stack) ->
        CauldronInteraction.fillBucket(state, level, pos, player, hand, stack, new ItemStack(Items.POWDER_SNOW_BUCKET),
            full -> full.getValue(LayeredCauldronBlock.LEVEL) == FULL, SoundEvents.BUCKET_FILL_POWDER_SNOW);
    /** Vanilla's, with the full check at four: past it, a bottle would wrap a full cauldron back to one level. */
    private static final CauldronInteraction POUR_WATER_BOTTLE = (state, level, pos, player, hand, stack) -> {
        if (state.getValue(LayeredCauldronBlock.LEVEL) == FULL)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
        if (potion == null || !potion.is(Potions.WATER))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            level.setBlockAndUpdate(pos, state.cycle(LayeredCauldronBlock.LEVEL));
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    };

    private FourLevelInteractions() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener((FMLLoadCompleteEvent event) -> event.enqueueWork(FourLevelInteractions::swap));
    }

    private static void swap() {
        int fills = 0;
        for (CauldronInteraction.InteractionMap interactions : CauldronInteraction.INTERACTIONS.values()) {
            for (Map.Entry<Item, CauldronInteraction> interaction : interactions.map().entrySet()) {
                if (interaction.getValue() == CauldronInteraction.FILL_WATER) {
                    interaction.setValue(FILL_WATER);
                    fills++;
                } else if (interaction.getValue() == CauldronInteraction.FILL_POWDER_SNOW) {
                    interaction.setValue(FILL_POWDER_SNOW);
                    fills++;
                }
            }
        }
        swapIfVanilla(CauldronInteraction.WATER, Items.BUCKET, TAKE_WATER);
        swapIfVanilla(CauldronInteraction.WATER, Items.POTION, POUR_WATER_BOTTLE);
        swapIfVanilla(CauldronInteraction.POWDER_SNOW, Items.BUCKET, TAKE_POWDER_SNOW);
        Lifeguard.LOGGER.debug("Lifeguard: cauldron fourths swapped {} bucket fills across {} interaction maps", fills,
            CauldronInteraction.INTERACTIONS.size());
    }

    private static void swapIfVanilla(CauldronInteraction.InteractionMap interactions, Item item, CauldronInteraction fourths) {
        CauldronInteraction current = interactions.map().get(item);
        if (current != null && current.getClass().getName().startsWith(CauldronInteraction.class.getName() + "$$Lambda"))
            interactions.map().put(item, fourths);
        else
            Lifeguard.LOGGER.warn("Lifeguard: cauldron fourths left {} on {} cauldrons alone: another mod replaced it ({})",
                item, interactions.name(), current == null ? "removed" : current.getClass().getName());
    }
}
