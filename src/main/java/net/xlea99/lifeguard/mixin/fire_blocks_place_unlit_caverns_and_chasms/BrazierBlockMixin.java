package net.xlea99.lifeguard.mixin.fire_blocks_place_unlit_caverns_and_chasms;

import com.teamabnormals.caverns_and_chasms.common.block.BrazierBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.fire_blocks_place_unlit.FireBlocksPlaceUnlit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Caverns & Chasms' braziers place unlit, as campfires do ({@link FireBlocksPlaceUnlit}); flint and steel lights them. */
@Mixin(BrazierBlock.class)
public abstract class BrazierBlockMixin {
    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void lifeguard$placeUnlit(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        if (state != null && FireBlocksPlaceUnlit.CAVERNS_AND_CHASMS.active())
            cir.setReturnValue(state.setValue(BrazierBlock.LIT, false));
    }
}
