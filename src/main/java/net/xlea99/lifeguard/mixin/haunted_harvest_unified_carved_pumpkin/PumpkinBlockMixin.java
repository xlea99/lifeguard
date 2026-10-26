package net.xlea99.lifeguard.mixin.haunted_harvest_unified_carved_pumpkin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin.UnifiedPumpkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Shearing a pumpkin carves Haunted Harvest's pumpkin, vanilla's face, instead of vanilla's ({@link UnifiedPumpkin}). */
@Mixin(PumpkinBlock.class)
public abstract class PumpkinBlockMixin {

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean lifeguard$carveHauntedPumpkin(Level level, BlockPos pos, BlockState state, int flags, Operation<Boolean> original) {
        return UnifiedPumpkin.setBlock(level, pos, state, flags, (l, p, s, f) -> original.call(l, p, s, f));
    }
}
