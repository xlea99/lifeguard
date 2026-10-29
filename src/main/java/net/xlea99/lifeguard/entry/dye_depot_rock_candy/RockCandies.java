package net.xlea99.lifeguard.entry.dye_depot_rock_candy;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xlea99.lifeguard.Lifeguard;
import org.jetbrains.annotations.Nullable;

/**
 * Alex's Caves rock candy in Dye Depot's 16 colors: {@code lifeguard:rock_candy_<color>} ({@link DyeDepotRockCandy}).
 *
 * <p>Each one is one of Alex's Caves' own in another color: its block copies the properties of their rock candy and its
 * item their rock candy's food (edible, with its small chance of Sugar Rush), read off their registered white one, so
 * Lifeguard loads after Alex's Caves (an optional ordering in its mods.toml). They sit in their Candy Cavity tab after
 * their black one. Everything else is data: membership in {@code alexscaves:rock_candies} (pickaxe mining, the "dye any
 * rock candy" recipes, the biome treat, the rock candy advancement, the Confection Oven), both of their recipe shapes per
 * color, drop-self loot, and Moonlight's color set.
 */
public final class RockCandies {
    /** Dye Depot's colors, in its own order. */
    public static final List<String> COLORS = List.of("maroon", "rose", "coral", "indigo", "navy", "slate", "olive",
        "amber", "beige", "teal", "mint", "aqua", "verdant", "forest", "ginger", "tan");

    private static final ResourceLocation TEMPLATE = ResourceLocation.fromNamespaceAndPath("alexscaves", "rock_candy_white");
    private static final ResourceLocation TAB = ResourceLocation.fromNamespaceAndPath("alexscaves", "candy_cavity");
    private static final ResourceLocation TAB_AFTER = ResourceLocation.fromNamespaceAndPath("alexscaves", "rock_candy_black");

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Lifeguard.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Lifeguard.MODID);
    private static final List<DeferredItem<BlockItem>> CANDIES = new ArrayList<>();

    static {
        for (String color : COLORS) {
            String name = "rock_candy_" + color;
            DeferredBlock<Block> block = BLOCKS.register(name, () -> new Block(BlockBehaviour.Properties.ofFullCopy(template())));
            CANDIES.add(ITEMS.register(name, () -> new BlockItem(block.get(), itemProperties())));
        }
    }

    private RockCandies() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }

    public static void registerTab(IEventBus modBus) {
        modBus.addListener(RockCandies::addToCandyCavityTab);
    }

    /** One of these candies' Dye Depot color, or null for any other block. */
    @Nullable
    public static DyeColor colorOf(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (!id.getNamespace().equals(Lifeguard.MODID) || !id.getPath().startsWith("rock_candy_"))
            return null;
        String color = id.getPath().substring("rock_candy_".length());
        return COLORS.contains(color) ? DyeColor.byName(color, null) : null;
    }

    /** One of these candies, or null when it isn't registered (Alex's Caves or Dye Depot missing). */
    @Nullable
    public static Block block(String color) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "rock_candy_" + color));
        return block == Blocks.AIR ? null : block;
    }

    private static Block template() {
        Block block = BuiltInRegistries.BLOCK.get(TEMPLATE);
        if (block == Blocks.AIR)
            throw new IllegalStateException("Alex's Caves' rock candy isn't registered before Lifeguard's: check the load ordering");
        return block;
    }

    private static Item.Properties itemProperties() {
        Item.Properties properties = new Item.Properties();
        FoodProperties food = BuiltInRegistries.ITEM.get(TEMPLATE).components().get(DataComponents.FOOD);
        if (food != null)
            properties.food(food);
        return properties;
    }

    private static void addToCandyCavityTab(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().location().equals(TAB))
            return;
        ItemStack after = new ItemStack(BuiltInRegistries.ITEM.get(TAB_AFTER));
        for (DeferredItem<BlockItem> candy : CANDIES) {
            ItemStack stack = new ItemStack(candy.get());
            event.insertAfter(after, stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            after = stack;
        }
    }
}
