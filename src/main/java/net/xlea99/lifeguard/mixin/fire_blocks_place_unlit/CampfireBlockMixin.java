package net.xlea99.lifeguard.mixin.fire_blocks_place_unlit;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.fire_blocks_place_unlit.FireBlocksPlaceUnlit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Campfires place unlit, so lighting one takes flint and steel (or a fire charge, a flaming arrow...), as Forgery's
 * {@code campfires_place_unlit} had it: the same hook, so every CampfireBlock (vanilla's two and any mod's built on it).
 */
@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin {
    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void lifeguard$placeUnlit(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        if (state != null && FireBlocksPlaceUnlit.ENTRY.active())
            cir.setReturnValue(state.setValue(CampfireBlock.LIT, false));
    }
}
