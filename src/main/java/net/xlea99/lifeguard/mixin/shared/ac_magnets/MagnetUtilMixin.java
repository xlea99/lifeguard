package net.xlea99.lifeguard.mixin.shared.ac_magnets;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.xlea99.lifeguard.magnet.Magnets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Our neodymium blocks as Alex's Caves magnets ({@link Magnets}), for every entry with them in it. They're in AC's own
 * magnet points of interest, so AC pulls toward each: one lambda per magnet, attracting ({@code $2}) and repelling
 * ({@code $3}), each scaling a direction by distance and adding it to the entity's magnetic motion. Ours pull at half
 * that, and an off one not at all (skipped whole, so it doesn't damp the motion either). What's pulled can walk on a
 * scarlet one, as on AC's own, which only {@code #alexscaves:magnetic_attachables} lists.
 *
 * <p>Written against Alex's official 1.21.1 branch (2.0.2, the build Deep End runs). If Alex's release changes how
 * magnets are summed, this needs remaking.
 */
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.entity.util.MagnetUtil")
public abstract class MagnetUtilMixin {

    @Inject(method = {"lambda$tickMagnetism$2", "lambda$tickMagnetism$3"}, at = @At("HEAD"), cancellable = true)
    private static void lifeguard$offMagnetsDontPull(Entity entity, int range, BlockPos magnet, CallbackInfo ci) {
        BlockState state = entity.level().getBlockState(magnet);
        if (Magnets.isOurs(state) && Magnets.pull(state) == 0)
            ci.cancel();
    }

    @WrapOperation(method = {"lambda$tickMagnetism$2", "lambda$tickMagnetism$3"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;", ordinal = 0))
    private static Vec3 lifeguard$halfStrength(Vec3 direction, double factor, Operation<Vec3> original,
                                               @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) BlockPos magnet) {
        boolean ours = Magnets.isOurs(entity.level().getBlockState(magnet));
        return original.call(direction, ours ? factor * Magnets.STRENGTH : factor);
    }

    @WrapOperation(method = {"getStandingOnMagnetSurface", "calculateClosestDirection"}, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean lifeguard$scarletAttaches(BlockState state, TagKey<Block> attachables, Operation<Boolean> original) {
        return original.call(state, attachables) || Magnets.pull(state) > 0;
    }
}
