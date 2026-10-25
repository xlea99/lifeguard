package net.xlea99.lifeguard.entry.supps_moving_block_contents;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;

/**
 * The block a piston or pulley is drawing right now, when Supplementaries carries its contents along: set around the
 * draw, read by the model drawing under it. Render thread only.
 */
public final class MovingContents {
    @Nullable
    private static BlockEntity carried;
    @Nullable
    private static BlockPos drawnAt;

    private MovingContents() {
    }

    public static void begin(BlockEntity contents, BlockPos pos) {
        carried = contents;
        drawnAt = pos.immutable();
    }

    public static void end() {
        carried = null;
        drawnAt = null;
    }

    /** The contents for a block drawn at {@code pos} as {@code state}, or null. */
    @Nullable
    public static BlockEntity at(BlockPos pos, BlockState state) {
        return carried != null && pos.equals(drawnAt) && carried.getBlockState().is(state.getBlock()) ? carried : null;
    }

    /** The world as the moving block's model and colors see it: its contents are where it's drawn. */
    public static BlockAndTintGetter withContents(BlockAndTintGetter level, BlockPos pos, BlockEntity contents) {
        return new BlockAndTintGetter() {
            @Override
            public float getShade(Direction direction, boolean shade) {
                return level.getShade(direction, shade);
            }

            @Override
            public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
                return level.getShade(normalX, normalY, normalZ, shade);
            }

            @Override
            public LevelLightEngine getLightEngine() {
                return level.getLightEngine();
            }

            @Override
            public int getBlockTint(BlockPos at, ColorResolver resolver) {
                return level.getBlockTint(at, resolver);
            }

            @Nullable
            @Override
            public BlockEntity getBlockEntity(BlockPos at) {
                return at.equals(pos) ? contents : level.getBlockEntity(at);
            }

            @Override
            public BlockState getBlockState(BlockPos at) {
                return level.getBlockState(at);
            }

            @Override
            public FluidState getFluidState(BlockPos at) {
                return level.getFluidState(at);
            }

            @Override
            public int getHeight() {
                return level.getHeight();
            }

            @Override
            public int getMinBuildHeight() {
                return level.getMinBuildHeight();
            }

            @Nullable
            @Override
            public AuxiliaryLightManager getAuxLightManager(ChunkPos at) {
                return level.getAuxLightManager(at);
            }

            @Override
            public ModelData getModelData(BlockPos at) {
                return at.equals(pos) ? contents.getModelData() : level.getModelData(at);
            }
        };
    }
}
