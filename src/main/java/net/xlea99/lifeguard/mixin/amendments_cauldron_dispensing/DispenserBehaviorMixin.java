package net.xlea99.lifeguard.mixin.amendments_cauldron_dispensing;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.amendments.events.behaviors.CauldronConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A dispenser facing an empty cauldron pours a potion, dye, milk or stew into it again, making an Amendments cauldron,
 * as it did on 1.20.1. Amendments checks whether the DISPENSER is a cauldron (it never is), so it never poured; and if
 * a pour failed, it put the dispenser's own block where the cauldron was. Both now look at the cauldron.
 */
@Mixin(CauldronConversion.DispenserBehavior.class)
public abstract class DispenserBehaviorMixin {

    @WrapOperation(method = "customBehavior",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean lifeguard$isTheCauldron(BlockState dispenser, Block cauldron, Operation<Boolean> original,
                                            @Local ServerLevel level, @Local BlockPos pos) {
        return original.call(level.getBlockState(pos), cauldron);
    }

    @WrapOperation(method = "customBehavior",
        at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean lifeguard$putTheCauldronBack(ServerLevel level, BlockPos pos, BlockState dispenser,
                                                 Operation<Boolean> original) {
        return original.call(level, pos, Blocks.CAULDRON.defaultBlockState());
    }
}
