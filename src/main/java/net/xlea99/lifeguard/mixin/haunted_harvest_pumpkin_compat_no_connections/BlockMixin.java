package net.xlea99.lifeguard.mixin.haunted_harvest_pumpkin_compat_no_connections;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.haunted_harvest_pumpkin_compat.Pumpkins;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fences, walls, panes and bars keep away from Haunted Harvest's pumpkins as from vanilla's ({@link Pumpkins}): vanilla
 * lists its carved pumpkin and jack o'lantern in this one shared "never connect to these" check.
 */
@Mixin(Block.class)
public abstract class BlockMixin {

    @ModifyReturnValue(method = "isExceptionForConnection", at = @At("RETURN"))
    private static boolean lifeguard$hauntedPumpkinsToo(boolean exception, @Local(argsOnly = true) BlockState state) {
        return exception || Pumpkins.isPumpkin(state.getBlock());
    }
}
