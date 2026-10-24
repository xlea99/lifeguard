package net.xlea99.lifeguard.mixin.themed_candle_holder_flames;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CandleHolderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.themed_candle_holder_flames.HolderFlames;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Soul flames for candle holders mounted on soul sand or soul soil ({@link HolderFlames}). */
@Mixin(CandleHolderBlock.class)
public abstract class CandleHolderBlockMixin {

    @Inject(method = "animateTick", at = @At("HEAD"))
    private void lifeguard$findSupport(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        HolderFlames.spawning(state, pos);
    }

    @Inject(method = "animateTick", at = @At("RETURN"))
    private void lifeguard$forgetSupport(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        HolderFlames.spawning(null, null);
    }

    @WrapOperation(method = "addParticlesAndSound", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void lifeguard$soulFlames(Level level, ParticleOptions particle, double x, double y, double z, double dx, double dy,
                                      double dz, Operation<Void> original) {
        BlockPos support = HolderFlames.support();
        original.call(level, support != null ? HolderFlames.soul(particle, level, support) : particle, x, y, z, dx, dy, dz);
    }
}
