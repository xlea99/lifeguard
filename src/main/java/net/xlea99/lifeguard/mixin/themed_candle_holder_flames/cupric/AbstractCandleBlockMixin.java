package net.xlea99.lifeguard.mixin.themed_candle_holder_flames.cupric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.xlea99.lifeguard.entry.themed_candle_holder_flames.CupricFlames;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Cupric flames for candles and candle cakes on copper ({@link CupricFlames}). Wraps the flame's particle call the way
 * Quark's Soul Candles does (a wrap, so both chain), and only ever touches the plain small flame, so a candle Quark
 * already turned to soul fire stays soul fire.
 */
@Mixin(AbstractCandleBlock.class)
public abstract class AbstractCandleBlockMixin {

    @WrapOperation(method = "addParticlesAndSound", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private static void lifeguard$cupricFlames(Level level, ParticleOptions particle, double x, double y, double z, double dx, double dy,
                                               double dz, Operation<Void> original) {
        original.call(level, CupricFlames.particleAt(particle, level, x, y, z), x, y, z, dx, dy, dz);
    }
}
