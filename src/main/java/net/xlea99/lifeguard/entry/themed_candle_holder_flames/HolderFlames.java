package net.xlea99.lifeguard.entry.themed_candle_holder_flames;

import net.mehvahdjukaar.supplementaries.common.block.blocks.CandleHolderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.content.client.module.SoulCandlesModule;

/**
 * Supplementaries' candle holders (Supplementaries Squared's gold ones are the same block class), themed by what they're
 * mounted on. They don't extend vanilla's candle block and spawn their own flames, so Quark's Soul Candles never reaches
 * them. Quark judges a vanilla candle by the block under its flame, but a holder's flames can sit above its own block
 * (tall candles), and it can hang from walls and ceilings, so a holder is judged by the block it's mounted on instead.
 */
public final class HolderFlames {
    /** The block the holder now spawning particles is mounted on. Particles are spawned on the client thread only. */
    @Nullable
    private static BlockPos support;

    private HolderFlames() {
    }

    @Nullable
    public static BlockPos support() {
        return support;
    }

    /** Below a floor holder, behind a wall one, above a ceiling one; null once the holder's done. */
    public static void spawning(@Nullable BlockState holder, @Nullable BlockPos pos) {
        if (holder == null || pos == null) {
            support = null;
            return;
        }
        Direction towardSupport = switch (holder.getValue(CandleHolderBlock.FACE)) {
            case FLOOR -> Direction.DOWN;
            case CEILING -> Direction.UP;
            case WALL -> holder.getValue(CandleHolderBlock.FACING).getOpposite();
        };
        support = pos.relative(towardSupport);
    }

    /**
     * Quark's Soul Candles for a holder: soul flames (and the odd soul for smoke) on soul sand or soul soil, by Quark's
     * own check and its own toggle. Quark looks one block below the flame it's given, so the "flame" goes just above the
     * support.
     */
    public static ParticleOptions soul(ParticleOptions particle, Level level, BlockPos support) {
        return SoulCandlesModule.getParticleOptions(particle, level, support.getX() + 0.5, support.getY() + 1.5, support.getZ() + 0.5);
    }
}
