package net.xlea99.lifeguard.block;

import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.violetmoon.quark.content.building.block.VerticalSlabBlock;
import org.violetmoon.quark.content.building.module.VerticalSlabsModule;

/** Quark's own vertical slabs for our blocks. Its own class so Quark's classes only ever load when Quark is installed. */
public final class QuarkVerticalSlabs {
    private QuarkVerticalSlabs() {
    }

    /** Quark's vertical slab block on our slab's properties: its placing, doubling and waterlogging. */
    public static Block create(Supplier<Block> slab, BlockBehaviour.Properties properties) {
        return new VerticalSlabBlock(slab, properties);
    }

    /** Whether Quark's Vertical Slabs module is on (its recipes ask the same, through zeta:flag). */
    public static boolean on() {
        return VerticalSlabsModule.staticEnabled;
    }
}
