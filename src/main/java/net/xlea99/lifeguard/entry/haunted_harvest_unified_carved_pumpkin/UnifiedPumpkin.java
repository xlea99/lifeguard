package net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin;

import net.mehvahdjukaar.hauntedharvest.blocks.ModCarvedPumpkinBlock;
import net.mehvahdjukaar.hauntedharvest.blocks.ModCarvedPumpkinBlockTile;
import net.mehvahdjukaar.hauntedharvest.entity.ICustomPumpkinHolder;
import net.mehvahdjukaar.hauntedharvest.items.components.PumpkinCarvingData;
import net.mehvahdjukaar.hauntedharvest.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.xlea99.lifeguard.Lifeguard;

/**
 * Haunted Harvest's carvable pumpkins are THE carved pumpkins ({@link HauntedHarvestUnifiedCarvedPumpkin}): where a
 * vanilla carved pumpkin or jack o'lantern would come from, Haunted Harvest's comes instead, carved with vanilla's face
 * ({@link #VANILLA_FACE}).
 *
 * <p>Sources: shearing a pumpkin ({@code mixin...PumpkinBlockMixin}), Haunted Harvest's own picks of a vanilla one
 * ({@code mixin...CustomCarvingsManagerMixin}), anything wearing one when it joins a world (Halloween zombies and
 * skeletons, anything else; here), snow golems that have a pumpkin but no Haunted Harvest one (spawn eggs, natural ones;
 * here, through Haunted Harvest's own golem pumpkins), Supplementaries' Halloween statues (child), EMI's shearing recipe
 * (child) and worldgen, every structure and feature ({@code mixin...WorldGenRegionMixin}). Worldgen's and mobs' heads'
 * carvings come from the pack's {@link CarvingPools}; everywhere else, and anywhere no pool covers, vanilla's face.
 *
 * <p>Vanilla's face is also Haunted Harvest's: the preset {@code lifeguard:vanilla} (kids and players can carve it) and
 * what Haunted Harvest itself reaches for as "the vanilla face" (its own {@code classic} is a simpler face of its own).
 * The carving is always written out, so a Haunted Harvest pumpkin with no carving stays blank, as its knife flow starts it.
 */
public final class UnifiedPumpkin {
    /** Vanilla's carved pumpkin face as a Haunted Harvest carving (its 82 dark pixels, exactly), packed as Haunted Harvest packs them. */
    public static final long[] VANILLA_FACE = {4499109221882658816L, 2017679119407127804L, 4537408493710740992L, 26388794969328L};
    /** The same face as Haunted Harvest's preset ({@code data/lifeguard/pumpkin_carvings/vanilla.json}). */
    public static final ResourceLocation VANILLA_PRESET = ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "vanilla");

    private UnifiedPumpkin() {
    }

    public static boolean active() {
        return HauntedHarvestUnifiedCarvedPumpkin.ENTRY.active();
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(UnifiedPumpkin::onEntityJoin);
    }

    public static boolean isVanilla(BlockState state) {
        return state.is(Blocks.CARVED_PUMPKIN) || state.is(Blocks.JACK_O_LANTERN);
    }

    public static boolean isVanilla(ItemStack stack) {
        return stack.is(Items.CARVED_PUMPKIN) || stack.is(Items.JACK_O_LANTERN);
    }

    private static ModCarvedPumpkinBlock hauntedBlock(boolean lit) {
        return (ModCarvedPumpkinBlock) (lit ? ModRegistry.JACK_O_LANTERN : ModRegistry.CARVED_PUMPKIN).get();
    }

    /** Haunted Harvest's pumpkin (lit like the vanilla one) in place of a vanilla pumpkin block, facing the same way. */
    public static BlockState hauntedState(BlockState vanilla) {
        return hauntedBlock(vanilla.is(Blocks.JACK_O_LANTERN)).defaultBlockState()
            .setValue(CarvedPumpkinBlock.FACING, vanilla.getValue(CarvedPumpkinBlock.FACING));
    }

    /** Haunted Harvest's pumpkin item (lit like the vanilla one) carved {@code pixels}, in place of a vanilla one. */
    public static ItemStack hauntedItem(ItemStack vanilla, long[] pixels) {
        ModCarvedPumpkinBlock block = hauntedBlock(vanilla.is(Items.JACK_O_LANTERN));
        ItemStack stack = new ItemStack(block, vanilla.getCount());
        stack.set(ModRegistry.PUMPKIN_CARVING.get(),
            PumpkinCarvingData.of(PumpkinCarvingData.unpack(pixels), block.getType(block.defaultBlockState()), false));
        return stack;
    }

    /**
     * Carves the Haunted Harvest pumpkin block at {@code pos} {@code pixels}. In worldgen the block entity is the chunk's
     * own, still being generated: there's no one to tell yet, the chunk goes out whole.
     */
    public static void carve(LevelAccessor level, BlockPos pos, long[] pixels) {
        if (!(level.getBlockEntity(pos) instanceof ModCarvedPumpkinBlockTile tile))
            return;
        tile.setPixels(PumpkinCarvingData.unpack(pixels));
        if (level instanceof WorldGenRegion)
            return;
        tile.setChanged();
        if (tile.getLevel() != null)
            tile.getLevel().sendBlockUpdated(pos, tile.getBlockState(), tile.getBlockState(), 3);
    }

    /**
     * Places Haunted Harvest's pumpkin where a vanilla one was being placed, carved with vanilla's face (in worldgen,
     * from the pool for what's generating); the original placement otherwise.
     */
    public static boolean setBlock(LevelAccessor level, BlockPos pos, BlockState state, int flags, PlaceBlock original) {
        if (!isVanilla(state) || !active())
            return original.place(level, pos, state, flags);
        boolean placed = original.place(level, pos, hauntedState(state), flags);
        if (placed)
            carve(level, pos, level instanceof WorldGenRegion region ? CarvingPools.forWorldgen(region, pos) : VANILLA_FACE);
        return placed;
    }

    @FunctionalInterface
    public interface PlaceBlock {
        boolean place(LevelAccessor level, BlockPos pos, BlockState state, int flags);
    }

    private static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof LivingEntity living) || living instanceof Player || !active())
            return;
        ItemStack head = living.getItemBySlot(EquipmentSlot.HEAD);
        if (isVanilla(head))
            living.setItemSlot(EquipmentSlot.HEAD, hauntedItem(head, CarvingPools.forMobHead(living.getRandom())));
        // A snow golem with a pumpkin but no Haunted Harvest one wears vanilla's face, as Haunted Harvest draws and drops it.
        if (living instanceof SnowGolem golem && golem.hasPumpkin() && golem instanceof ICustomPumpkinHolder holder
            && holder.hauntedharvest$getCustomPumpkin().isEmpty())
            holder.hauntedharvest$setCustomPumpkin(hauntedItem(new ItemStack(Items.CARVED_PUMPKIN), VANILLA_FACE));
    }
}
