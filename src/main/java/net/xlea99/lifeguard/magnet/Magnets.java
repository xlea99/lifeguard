package net.xlea99.lifeguard.magnet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.world.poi.ExtendPoiTypesEvent;
import net.xlea99.lifeguard.core.Entry;

/**
 * Our neodymium blocks are Alex's Caves magnets: scarlet ones attract, azure ones repel, and what's pulled can walk on
 * scarlet ones. They join AC's own magnet points of interest, so AC's own code finds and pulls toward them; the
 * {@code shared.ac_magnets} mixin then pulls at half a neodymium block's strength (less metal in a brick or a bar), and
 * not at all while the block's entry is off.
 *
 * <p>Joining is registration, so it follows the mods installed, never a switch; the switch is checked per pull.
 */
public final class Magnets {
    private static final ResourceKey<PoiType> ATTRACTING = poi("attracting_magnets");
    private static final ResourceKey<PoiType> REPELLING = poi("repelling_magnets");
    /** What our blocks pull at, against an AC neodymium block's. */
    public static final double STRENGTH = 0.5;

    /** {@code pull}: 1 attracts, -1 repels. */
    private record Magnet(Supplier<? extends Block> block, int pull, Entry entry) {
    }

    private static final List<Magnet> MAGNETS = new ArrayList<>();

    private Magnets() {
    }

    /** During mod construction, by the content with magnets in it. */
    public static void add(Supplier<? extends Block> block, int pull, Entry entry) {
        MAGNETS.add(new Magnet(block, pull, entry));
    }

    /** After every piece of content has added its magnets. */
    public static void register(IEventBus modBus) {
        if (!MAGNETS.isEmpty() && ModList.get().isLoaded("alexscaves"))
            modBus.addListener(Magnets::extendPoiTypes);
    }

    private static void extendPoiTypes(ExtendPoiTypesEvent event) {
        for (Magnet magnet : MAGNETS)
            event.addBlockToPoi(magnet.pull() > 0 ? ATTRACTING : REPELLING, magnet.block().get());
    }

    /** Whether this is one of our magnets at all, on or off. */
    public static boolean isOurs(BlockState state) {
        for (Magnet magnet : MAGNETS)
            if (state.is(magnet.block().get()))
                return true;
        return false;
    }

    /** 1 if this attracts, -1 if it repels, 0 if it's no magnet of ours or its entry is off. */
    public static int pull(BlockState state) {
        for (Magnet magnet : MAGNETS)
            if (state.is(magnet.block().get()))
                return magnet.entry().active() ? magnet.pull() : 0;
        return 0;
    }

    private static ResourceKey<PoiType> poi(String path) {
        return ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ResourceLocation.fromNamespaceAndPath("alexscaves", path));
    }
}
