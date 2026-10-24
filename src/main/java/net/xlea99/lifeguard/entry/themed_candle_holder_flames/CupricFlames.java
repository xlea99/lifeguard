package net.xlea99.lifeguard.entry.themed_candle_holder_flames;

import com.teamabnormals.caverns_and_chasms.core.other.tags.CCBlockTags;
import com.teamabnormals.caverns_and_chasms.core.registry.CCParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Candles on copper burn with cupric flames, as Quark's Soul Candles makes candles on soul sand burn with soul flames: a
 * candle resting on anything cupric fire burns on ({@code #caverns_and_chasms:cupric_fire_base_blocks}: copper blocks,
 * waxed or not, copper ore, blocks of raw copper) swaps its flame for Caverns & Chasms' small cupric flame. Only the
 * flame changes, not the smoke or the light. Found the way Quark finds soul sand, so the two read the same: the block
 * under the flame, or under that if the flame's block is an enchanting power source (a bookshelf). Only loaded when the
 * child entry is active, so only with Caverns & Chasms installed.
 */
public final class CupricFlames {
    private CupricFlames() {
    }

    /** The particle a candle flame at (x, y, z) should really use. */
    public static ParticleOptions particleAt(ParticleOptions particle, Level level, double x, double y, double z) {
        if (particle != ParticleTypes.SMALL_FLAME)
            return particle;
        BlockPos under = BlockPos.containing(x, y - 1.0, z);
        BlockState state = level.getBlockState(under);
        if (!state.is(CCBlockTags.CUPRIC_FIRE_BASE_BLOCKS) && state.getEnchantPowerBonus(level, under) > 0.0F)
            state = level.getBlockState(under.below());
        return state.is(CCBlockTags.CUPRIC_FIRE_BASE_BLOCKS) ? CCParticleTypes.SMALL_CUPRIC_FIRE_FLAME.get() : particle;
    }

    /** Same, for a candle holder mounted on the block at {@code support}. */
    public static ParticleOptions particleOnSupport(ParticleOptions particle, Level level, BlockPos support) {
        return particleAt(particle, level, support.getX() + 0.5, support.getY() + 1.5, support.getZ() + 0.5);
    }
}
