package net.xlea99.lifeguard.mixin.fire_blocks_place_unlit.supplementaries;

import net.mehvahdjukaar.supplementaries.common.block.blocks.FirePitBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.xlea99.lifeguard.entry.fire_blocks_place_unlit.FireBlocksPlaceUnlit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Supplementaries' fire pit places unlit, as campfires do ({@link FireBlocksPlaceUnlit}); flint and steel lights it. */
@Mixin(FirePitBlock.class)
public abstract class FirePitBlockMixin {
    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void lifeguard$placeUnlit(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        if (state != null && FireBlocksPlaceUnlit.SUPPLEMENTARIES.active())
            cir.setReturnValue(state.setValue(BlockStateProperties.LIT, false));
    }
}
