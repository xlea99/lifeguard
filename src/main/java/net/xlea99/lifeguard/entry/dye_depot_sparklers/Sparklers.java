package net.xlea99.lifeguard.entry.dye_depot_sparklers;

import com.mojang.datafixers.util.Pair;
import com.teamabnormals.caverns_and_chasms.common.block.Sparkler;
import com.teamabnormals.caverns_and_chasms.common.block.SparklerBlock;
import com.teamabnormals.caverns_and_chasms.common.block.WallSparklerBlock;
import com.teamabnormals.caverns_and_chasms.core.registry.CCBlocks;
import com.teamabnormals.caverns_and_chasms.core.registry.CCSoundEvents;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xlea99.lifeguard.Lifeguard;

/**
 * Caverns & Chasms sparklers in Dye Depot's 16 colors: {@code lifeguard:<color>_sparkler} and its wall version
 * ({@link DyeDepotSparklers}).
 *
 * <p>Built from C&C's own parts, exactly as C&C builds its 16 (CCBlockSubRegistryHelper.createSparklerBlock): its
 * {@code SparklerBlock} and {@code WallSparklerBlock} on the same properties (light 12 while lit, its sparkler sounds),
 * its standing-and-wall item, and a spark and spark emitter particle pair per color, drawn by its own particle renderers
 * ({@link SparklersClient}). Joining C&C's dye map is all it takes for a dye on any sparkler to recolor it into ours. Data
 * does the rest: its sparkler tags, its recipe shape, drop-self loot, and Moonlight's color sets. The textures are C&C's
 * own, recolored at runtime (client/RecolorPack): its Abnormals License forbids shipping modified copies.
 */
public final class Sparklers {
    /** Dye Depot's colors, in its own order. */
    public static final List<String> COLORS = List.of("maroon", "rose", "coral", "indigo", "navy", "slate", "olive",
        "amber", "beige", "teal", "mint", "aqua", "verdant", "forest", "ginger", "tan");

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Lifeguard.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Lifeguard.MODID);
    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, Lifeguard.MODID);

    /** Each color's (standing, wall) sparkler, in Dye Depot's order. */
    private static final Map<String, Pair<DeferredBlock<SparklerBlock>, DeferredBlock<WallSparklerBlock>>> BY_COLOR = new LinkedHashMap<>();
    private static final List<DeferredItem<StandingAndWallBlockItem>> SPARKLER_ITEMS = new ArrayList<>();
    /** Each color's (spark, spark emitter) particle types, for {@link SparklersClient}. */
    static final List<Pair<DeferredHolder<ParticleType<?>, SimpleParticleType>, DeferredHolder<ParticleType<?>, SimpleParticleType>>> PARTICLE_PAIRS = new ArrayList<>();

    static {
        for (String color : COLORS) {
            String name = color + "_sparkler";
            DeferredHolder<ParticleType<?>, SimpleParticleType> spark = PARTICLES.register(name + "_spark", () -> new SimpleParticleType(true));
            DeferredHolder<ParticleType<?>, SimpleParticleType> emitter = PARTICLES.register(name + "_spark_emitter", () -> new SimpleParticleType(true));
            var particle = Pair.of(spark, emitter);
            PARTICLE_PAIRS.add(particle);
            DeferredBlock<SparklerBlock> block = BLOCKS.register(name, () -> new SparklerBlock(properties(), particle));
            DeferredBlock<WallSparklerBlock> wall = BLOCKS.register(color + "_wall_sparkler",
                () -> new WallSparklerBlock(properties().lootFrom(block), particle));
            SPARKLER_ITEMS.add(ITEMS.register(name,
                () -> new StandingAndWallBlockItem(block.get(), wall.get(), new Item.Properties(), Direction.DOWN)));
            BY_COLOR.put(color, Pair.of(block, wall));
        }
    }

    private Sparklers() {
    }

    /** Everything, whenever both mods are installed; the switch is checked where it applies (recipes are data). */
    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        PARTICLES.register(modBus);
        modBus.addListener(Sparklers::addToColoredBlocksTab);
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(Sparklers::joinDyeMap));
        NeoForge.EVENT_BUS.addListener(Sparklers::onRightClickBlock);
    }

    /**
     * C&C's own dye-on-sparkler handling (Sparkler.useSparkler) looks the dye's color up in its map of sparklers by
     * color, which only lists the 16 vanilla ones; ours join it. (Dye Depot's colors are DyeColor values by now: it
     * extends the enum before anything loads it.) The map is C&C's, read on both sides, so the switch is checked per
     * click instead ({@link #onRightClickBlock}).
     */
    private static void joinDyeMap() {
        BY_COLOR.forEach((color, pair) -> {
            DyeColor dye = DyeColor.byName(color, null);
            if (dye == null)
                Lifeguard.LOGGER.error("Lifeguard: Dye Depot's {} isn't a dye color; its sparkler can't be dyed into", color);
            else
                Sparkler.SPARKLER_BY_DYE.putIfAbsent(dye, pair);
        });
    }

    /** Off, a Dye Depot dye on a sparkler does what it did before: nothing to the sparkler. */
    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!DyeDepotSparklers.ENTRY.active() && event.getItemStack().getItem() instanceof DyeItem dye
            && BY_COLOR.containsKey(dye.getDyeColor().getSerializedName())
            && event.getLevel().getBlockState(event.getPos()).getBlock() instanceof Sparkler)
            event.setUseBlock(TriState.FALSE);
    }

    /** C&C's own sparkler properties (CCBlockSubRegistryHelper.createSparklerBlock). */
    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .instabreak()
            .lightLevel(CCBlocks.CCProperties.litBlockEmission(12))
            .sound(CCSoundEvents.CCSoundTypes.SPARKLER)
            .pushReaction(PushReaction.DESTROY);
    }

    private static void addToColoredBlocksTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.COLORED_BLOCKS || !DyeDepotSparklers.ENTRY.active())
            return;
        ItemStack after = new ItemStack(CCBlocks.PINK_SPARKLER.getFirst().get());
        for (DeferredItem<StandingAndWallBlockItem> sparkler : SPARKLER_ITEMS) {
            ItemStack stack = new ItemStack(sparkler.get());
            event.insertAfter(after, stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            after = stack;
        }
    }
}
