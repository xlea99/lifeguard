package net.xlea99.lifeguard.mixin.haunted_harvest_pumpkin_compat_species;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.haunted_harvest_pumpkin_compat.Pumpkins;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Species' Mammutilation ritual takes a Haunted Harvest pumpkin too ({@link Pumpkins}). The ritual is Species' own mixin
 * into the lightning rod ({@code LightningRodMixin.s$onLightningStrike}), which looks for vanilla's carved pumpkin by
 * block (and leaves it out of the body it consumes), so this wraps those checks inside Species' handler through
 * MixinSquared. Haunted Harvest's pumpkin is a carved pumpkin block, so the ritual's facing check reads it the same way.
 * Applied after Species' mixin (higher priority), whose handler this is.
 */
@Mixin(value = LightningRodBlock.class, priority = 1500)
public abstract class LightningRodBlockMixin {

    @TargetHandler(mixin = "com.ninni.species.mixin.LightningRodMixin", name = "s$onLightningStrike")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean lifeguard$hauntedPumpkinsCount(BlockState state, Block wanted, Operation<Boolean> original) {
        return original.call(state, wanted) || Pumpkins.standsInFor(wanted, state.getBlock());
    }
}
