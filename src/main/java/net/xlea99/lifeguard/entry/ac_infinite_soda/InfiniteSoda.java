package net.xlea99.lifeguard.entry.ac_infinite_soda;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.CreateFluidSourceEvent;

/**
 * Alex's Caves' purple soda as an infinite liquid, like water. A soda source already gives endless bottles, and Alex's
 * Caves tells Create's hose pulley soda is bottomless, but the liquid itself never formed new sources. Here it does,
 * the same way water does (two sources beside a flowing block over solid ground or another source). Reads Alex's Caves
 * only by name. Only loaded when the entry is active.
 */
public final class InfiniteSoda {
    private static final ResourceLocation SODA = ResourceLocation.fromNamespaceAndPath("alexscaves", "purple_soda");

    private InfiniteSoda() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(InfiniteSoda::onCreateFluidSource);
    }

    /** Whether this is purple soda, source or flowing. */
    public static boolean isSoda(FluidState fluid) {
        if (fluid.isEmpty())
            return false;
        Fluid soda = BuiltInRegistries.FLUID.get(SODA);
        return soda != Fluids.EMPTY && fluid.getType().isSame(soda);
    }

    /** Fired for each source beside a flowing block that checks whether to become one; the state is that source. */
    private static void onCreateFluidSource(CreateFluidSourceEvent event) {
        if (isSoda(event.getFluidState()))
            event.setCanConvert(true);
    }
}
