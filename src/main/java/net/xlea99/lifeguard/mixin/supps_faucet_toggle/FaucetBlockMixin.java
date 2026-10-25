package net.xlea99.lifeguard.mixin.supps_faucet_toggle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FaucetBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Turning a faucet on or off takes on the first click. A click saves the faucet toggled, then, if its "has water" was
 * out of date (a cauldron behind it changed without telling it, like water turned into a dye cauldron), saves it again
 * with the water corrected: but built from the faucet as it was before the click, so the second save undid the toggle.
 * The correction now starts from the faucet as it is.
 */
@Mixin(FaucetBlock.class)
public abstract class FaucetBlockMixin {

    @WrapOperation(method = "updateBlock",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean lifeguard$keepTheToggle(Level level, BlockPos pos, BlockState stale, Operation<Boolean> original) {
        BlockState current = level.getBlockState(pos);
        if (current.is(stale.getBlock()))
            return original.call(level, pos, current.setValue(FaucetBlock.HAS_WATER, stale.getValue(FaucetBlock.HAS_WATER)));
        return original.call(level, pos, stale);
    }
}
