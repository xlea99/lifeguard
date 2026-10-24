package net.xlea99.lifeguard.mixin.themed_candle_holder_flames.cupric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CandleHolderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.themed_candle_holder_flames.CupricFlames;
import net.xlea99.lifeguard.entry.themed_candle_holder_flames.HolderFlames;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Cupric flames for candle holders mounted on copper ({@link CupricFlames}). Chains with the parent's soul-flame wrap:
 * only the plain small flame is touched, and soul sand isn't copper, so the two never fight. The block the holder is
 * mounted on comes from the parent's mixin, always loaded alongside this one.
 */
@Mixin(CandleHolderBlock.class)
public abstract class CandleHolderBlockMixin {

    @WrapOperation(method = "addParticlesAndSound", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void lifeguard$cupricFlames(Level level, ParticleOptions particle, double x, double y, double z, double dx, double dy,
                                        double dz, Operation<Void> original) {
        BlockPos support = HolderFlames.support();
        original.call(level, support != null ? CupricFlames.particleOnSupport(particle, level, support) : particle, x, y, z, dx, dy, dz);
    }
}
