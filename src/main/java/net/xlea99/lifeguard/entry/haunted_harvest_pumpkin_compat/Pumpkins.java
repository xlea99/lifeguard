package net.xlea99.lifeguard.entry.haunted_harvest_pumpkin_compat;

import java.util.List;
import net.mehvahdjukaar.hauntedharvest.blocks.ModCarvedPumpkinBlock;
import net.mehvahdjukaar.hauntedharvest.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.util.BlockSnapshot;

/**
 * Haunted Harvest's carved pumpkins and jack o'lanterns work where vanilla's do ({@link HauntedHarvestPumpkinCompat}):
 * golems ({@code mixin.haunted_harvest_pumpkin_compat}), dispensers (here), and through the children Vanilla Backport's
 * creaking and Species' Mammutilation ritual. The tags (crows, gloomoths, knives, swords...) are plain data.
 *
 * <p>Haunted Harvest's pumpkins are its own blocks and items, not vanilla's, so anything that asks for vanilla's by name
 * misses them. Every lantern Haunted Harvest has (soul, redstone, cupric...) counts.
 */
public final class Pumpkins {
    private Pumpkins() {
    }

    public static boolean isPumpkin(Block block) {
        return block instanceof ModCarvedPumpkinBlock;
    }

    public static boolean isPumpkin(Item item) {
        return item instanceof BlockItem block && isPumpkin(block.getBlock());
    }

    /** Whether a vanilla check for {@code wanted} (a vanilla carved pumpkin) should also take {@code item}. */
    public static boolean standsInFor(Item wanted, Item item) {
        return wanted == Items.CARVED_PUMPKIN && isPumpkin(item);
    }

    /** Whether a vanilla check for {@code wanted} (a vanilla carved pumpkin block) should also take {@code block}. */
    public static boolean standsInFor(Block wanted, Block block) {
        return wanted == Blocks.CARVED_PUMPKIN && isPumpkin(block);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((FMLCommonSetupEvent event) ->
            event.enqueueWork(() -> DispenserBlock.registerBehavior(ModRegistry.CARVED_PUMPKIN.get(), new Dispense())));
    }

    /**
     * Vanilla's carved pumpkin dispenser behavior for Haunted Harvest's: in front of a golem's body it finishes the golem,
     * otherwise it goes on a mob's head. Haunted Harvest builds the golem as the pumpkin is placed and gives it the
     * pumpkin's carving, which is only written once the block is down; so the placement is deferred the way NeoForge
     * defers a player's (captured, the item's carving written, then placed for real), or the golem would come out blank.
     */
    private static final class Dispense extends OptionalDispenseItemBehavior {
        /** What a dispenser does with an item it has no behavior for (Haunted Harvest registers none for its pumpkins). */
        private final DefaultDispenseItemBehavior drop = new DefaultDispenseItemBehavior();

        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            if (!(stack.getItem() instanceof BlockItem item)) {
                this.setSuccess(true);
                return this.drop.dispense(source, stack);
            }
            ServerLevel level = source.level();
            Direction facing = source.state().getValue(DispenserBlock.FACING);
            BlockPos target = source.pos().relative(facing);
            if (level.isEmptyBlock(target) && item.getBlock() instanceof ModCarvedPumpkinBlock pumpkin && pumpkin.canSpawnGolem(level, target)) {
                placeAsAPlayerWould(level, target, facing, item, stack);
                stack.shrink(1);
                this.setSuccess(true);
            } else
                this.setSuccess(ArmorItem.dispenseArmor(source, stack));
            return stack;
        }

        private static void placeAsAPlayerWould(ServerLevel level, BlockPos target, Direction facing, BlockItem item, ItemStack stack) {
            ItemStack placed = stack.copyWithCount(1);
            level.captureBlockSnapshots = true;
            try {
                item.place(new DirectionalPlaceContext(level, target, facing, placed, facing.getOpposite()));
            } finally {
                level.captureBlockSnapshots = false;
            }
            List<BlockSnapshot> snapshots = List.copyOf(level.capturedBlockSnapshots);
            level.capturedBlockSnapshots.clear();
            for (BlockSnapshot snapshot : snapshots) {
                BlockPos pos = snapshot.getPos();
                BlockState before = snapshot.getState();
                BlockState now = level.getBlockState(pos);
                now.onPlace(level, pos, before, false);
                level.markAndNotifyBlock(pos, level.getChunkAt(pos), before, now, snapshot.getFlags(), 512);
            }
        }
    }
}
