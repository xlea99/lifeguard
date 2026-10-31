package net.xlea99.lifeguard.block;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.entry.cc_extended_plated_bricks.CcExtendedPlatedBricks;
import net.xlea99.lifeguard.magnet.Magnets;

/**
 * Caverns & Chasms' plated bricks for the metals it leaves out ({@code cc_extended_plated_bricks}, a child per mod): {@code
 * lifeguard:cc/<metal's mod>/<metal>_bricks} and its stairs, slab, wall, chiseled bricks and (with Quark) vertical slab.
 * Every form is the same metal: same sounds, and a neodymium one of any shape is a magnet ({@link Magnets}).
 *
 * <p>Built like C&C's own (CCProperties.platedBricks: strength 3/6, needs a pickaxe, basedrum), from its recipe shape (two
 * ingots, two deepslate), sounding like their metal's block, as C&C's each sound like theirs. The textures are C&C's
 * iron and chiseled silver plated bricks recolored at runtime (client/RecolorPack, specs {@code
 * lifeguard_recolors/cc_plated_bricks*.json}); the chiseled ones carry our own glyphs, painted into C&C's chiseled
 * frame: netherite and necromium an ouroboros turning sunwise (gold's, the sun's) and widdershins (silver's, the
 * moon's), bismuth its hopper-crystal spiral, scarlet and azure neodymium Fire and Water. Bismuth's shimmer in step
 * with Etcetera's bismuth block.
 *
 * <p>Each metal is registered whenever its entry is installed (a world may hold them). Off, its bricks leave the
 * creative tab, can't be crafted, and its neodymium ones stop being magnets.
 */
public final class PlatedBricks {
    /** {@code pull}: 1 attracts, -1 repels, 0 isn't a magnet. */
    private record Metal(Entry entry, String mod, String name, ResourceLocation block, MapColor color,
                         boolean fireResistant, int pull, List<DeferredBlock<Block>> blocks) {
        Metal(Entry entry, String mod, String name, String block, MapColor color, boolean fireResistant, int pull) {
            this(entry, mod, name, ResourceLocation.parse(block), color, fireResistant, pull, new ArrayList<>());
        }
    }

    private static final List<Metal> METALS = List.of(
        new Metal(CcExtendedPlatedBricks.ENTRY, "minecraft", "netherite", "minecraft:netherite_block",
            MapColor.COLOR_BLACK, true, 0),
        new Metal(CcExtendedPlatedBricks.ENTRY, "caverns_and_chasms", "necromium", "caverns_and_chasms:necromium_block",
            MapColor.TERRACOTTA_GREEN, true, 0),
        new Metal(CcExtendedPlatedBricks.ETCETERA, "etcetera", "bismuth", "etcetera:bismuth_block",
            MapColor.METAL, false, 0),
        new Metal(CcExtendedPlatedBricks.ALEXSCAVES, "alexscaves", "azure_neodymium", "alexscaves:block_of_azure_neodymium",
            MapColor.COLOR_BLUE, false, -1),
        new Metal(CcExtendedPlatedBricks.ALEXSCAVES, "alexscaves", "scarlet_neodymium",
            "alexscaves:block_of_scarlet_neodymium", MapColor.COLOR_RED, false, 1));

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Lifeguard.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Lifeguard.MODID);

    private PlatedBricks() {
    }

    /** Every metal whose entry is installed. */
    public static void register(IEventBus modBus) {
        boolean any = false;
        for (Metal metal : METALS)
            if (metal.entry().installed()) {
                registerAll(metal);
                any = true;
            }
        if (!any)
            return;
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(PlatedBricks::addToTab);
    }

    private static void registerAll(Metal metal) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
            .mapColor(metal.color())
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(3.0F, 6.0F)
            .sound(new BorrowedSoundType(metal.block()));
        String prefix = "cc/" + metal.mod() + "/";
        String name = metal.name();
        DeferredBlock<Block> bricks = BLOCKS.register(prefix + name + "_bricks", () -> new Block(properties));
        metal.blocks().add(bricks);
        metal.blocks().add(BLOCKS.register(prefix + name + "_brick_stairs",
            () -> new StairBlock(bricks.get().defaultBlockState(), properties)));
        DeferredBlock<Block> slab = BLOCKS.register(prefix + name + "_brick_slab", () -> new SlabBlock(properties));
        metal.blocks().add(slab);
        if (ModList.get().isLoaded("quark"))
            metal.blocks().add(BLOCKS.register(prefix + name + "_brick_vertical_slab",
                () -> QuarkVerticalSlabs.create(slab, properties)));
        metal.blocks().add(BLOCKS.register(prefix + name + "_brick_wall", () -> new WallBlock(properties)));
        metal.blocks().add(BLOCKS.register(prefix + "chiseled_" + name + "_bricks", () -> new Block(properties)));
        for (DeferredBlock<Block> block : metal.blocks()) {
            ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(),
                metal.fireResistant() ? new Item.Properties().fireResistant() : new Item.Properties()));
            if (metal.pull() != 0)
                Magnets.add(block, metal.pull(), metal.entry());
        }
    }

    /** Each metal's after its metal's block, as C&C places its own; or after the metal before it, if that block isn't there. */
    private static void addToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.BUILDING_BLOCKS)
            return;
        ItemStack after = null;
        for (Metal metal : METALS) {
            if (metal.blocks().isEmpty())
                continue;
            ItemStack block = new ItemStack(BuiltInRegistries.ITEM.get(metal.block()));
            if (!block.isEmpty() && event.getParentEntries().contains(block))
                after = block;
            if (!metal.entry().active())
                continue;
            for (DeferredBlock<Block> brick : metal.blocks()) {
                if (brick.getId().getPath().endsWith("_vertical_slab") && !QuarkVerticalSlabs.on())
                    continue;
                ItemStack stack = new ItemStack(brick.get());
                if (after == null)
                    event.accept(stack);
                else
                    event.insertAfter(after, stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                after = stack;
            }
        }
    }
}
