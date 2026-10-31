package net.xlea99.lifeguard.entry.extended_metal_bars;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.block.BorrowedSoundType;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.magnet.Magnets;

/**
 * Bars for the metals without them ({@link ExtendedMetalBars}, a child per mod): {@code lifeguard:<metal's mod>/<metal>_bars}. Each metal's
 * bars have their own shape, as iron's, tin's, silver's, gold's, copper's and bismuth's do: netherite's are heavy
 * straight bars tied by short rungs at alternating heights; Caverns & Chasms' necromium's are a ribcage (spine,
 * breastbone shared with the next pane, drooping ribs); Alex's Caves' azure neodymium's are magnetic field lines bowing
 * away from a central bar, scarlet's the same lines pinched in toward it, each hung from a beam at the top. Vanilla's
 * iron bars otherwise (strength 5/6, needs a pickaxe), sounding like their metal's block; neodymium ones are magnets
 * ({@link Magnets}). The textures are our own.
 *
 * <p>Each is registered whenever its entry is installed (a world may hold them). Off, they leave the creative tab,
 * can't be crafted and stop being magnets.
 */
public final class Bars {
    /** {@code pull}: 1 attracts, -1 repels, 0 isn't a magnet. */
    private record Metal(Entry entry, String mod, String name, ResourceLocation block, MapColor color, boolean fireResistant,
                         int pull) {
        Metal(Entry entry, String mod, String name, String block, MapColor color, boolean fireResistant, int pull) {
            this(entry, mod, name, ResourceLocation.parse(block), color, fireResistant, pull);
        }
    }

    private record Listed(Entry entry, DeferredItem<BlockItem> item) {
    }

    private static final List<Metal> METALS = List.of(
        new Metal(ExtendedMetalBars.ENTRY, "minecraft", "netherite", "minecraft:netherite_block", MapColor.COLOR_BLACK,
            true, 0),
        new Metal(ExtendedMetalBars.CAVERNS_AND_CHASMS, "caverns_and_chasms", "necromium", "caverns_and_chasms:necromium_block",
            MapColor.TERRACOTTA_GREEN, true, 0),
        new Metal(ExtendedMetalBars.ALEXSCAVES, "alexscaves", "azure_neodymium", "alexscaves:block_of_azure_neodymium",
            MapColor.COLOR_BLUE, false, -1),
        new Metal(ExtendedMetalBars.ALEXSCAVES, "alexscaves", "scarlet_neodymium", "alexscaves:block_of_scarlet_neodymium",
            MapColor.COLOR_RED, false, 1));

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Lifeguard.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Lifeguard.MODID);
    private static final List<Listed> TAB_ORDER = new ArrayList<>();

    private Bars() {
    }

    /** Every metal whose entry is installed. */
    public static void register(IEventBus modBus) {
        for (Metal metal : METALS)
            if (metal.entry().installed())
                registerBars(metal);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(Bars::addToTab);
    }

    private static void registerBars(Metal metal) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
            .mapColor(metal.color())
            .requiresCorrectToolForDrops()
            .strength(5.0F, 6.0F)
            .sound(new BorrowedSoundType(metal.block()))
            .noOcclusion();
        String id = metal.mod() + "/" + metal.name() + "_bars";
        DeferredBlock<IronBarsBlock> bars = BLOCKS.register(id, () -> new IronBarsBlock(properties));
        TAB_ORDER.add(new Listed(metal.entry(), ITEMS.register(id, () -> new BlockItem(bars.get(),
            metal.fireResistant() ? new Item.Properties().fireResistant() : new Item.Properties()))));
        if (metal.pull() != 0)
            Magnets.add(bars, metal.pull(), metal.entry());
    }

    /**
     * After iron bars, in whichever category tab has them, as C&C puts its bars after their metal's. Never the search
     * tab: it's every other tab's items (ours already among them), and NeoForge builds it with this same event.
     */
    private static void addToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab().getType() != CreativeModeTab.Type.CATEGORY)
            return;
        ItemStack after = new ItemStack(Items.IRON_BARS);
        if (!event.getParentEntries().contains(after))
            return;
        for (Listed bars : TAB_ORDER) {
            if (!bars.entry().active())
                continue;
            ItemStack stack = new ItemStack(bars.item().get());
            event.insertAfter(after, stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            after = stack;
        }
    }
}
