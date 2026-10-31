package net.xlea99.lifeguard.entry.twigs_cc_flowering_azaleas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A tree grown from a flowering azalea gets Twigs' azalea flowers on its Caverns & Chasms azalea logs, an even chance
 * per side open to the air ({@link TwigsCcFloweringAzaleas}; {@code mixin.twigs_cc_flowering_azaleas.TreeGrowerMixin}).
 * Caverns & Chasms makes the azalea tree's trunk its own azalea log. Both mods' blocks are looked up by name, so neither
 * mod's classes are loaded here.
 */
public final class FloweringAzaleaTrees {
    private static final ResourceLocation LOG = ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "azalea_log");
    private static final ResourceLocation FLOWERS = ResourceLocation.fromNamespaceAndPath("twigs", "azalea_flowers");

    private FloweringAzaleaTrees() {
    }

    /** Flowers the logs of the tree that just grew at {@code origin} (the bush's spot, its trunk's base). */
    public static void flower(ServerLevel level, BlockPos origin, RandomSource random) {
        if (!TwigsCcFloweringAzaleas.ENTRY.active())
            return;
        Block log = BuiltInRegistries.BLOCK.get(LOG);
        if (log == Blocks.AIR || !(BuiltInRegistries.BLOCK.get(FLOWERS) instanceof MultifaceBlock flowers))
            return;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-4, 0, -4), origin.offset(4, 10, 4))) {
            if (!level.getBlockState(pos).is(log))
                continue;
            for (Direction side : Direction.values()) {
                if (random.nextBoolean())
                    continue;
                BlockPos at = pos.relative(side);
                BlockState there = level.getBlockState(at);
                if (!there.isAir() && !there.is(flowers))
                    continue;
                BlockState base = there.is(flowers) ? there : flowers.defaultBlockState();
                // The flowers' face toward the log they hang on.
                level.setBlock(at, base.setValue(MultifaceBlock.getFaceProperty(side.getOpposite()), true), Block.UPDATE_ALL);
            }
        }
    }
}
