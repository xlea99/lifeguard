package net.xlea99.lifeguard.mixin.supplementaries_grouper_open_sides;

import net.mehvahdjukaar.supplementaries.common.block.blocks.GrouperBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.supplementaries_grouper_open_sides.SupplementariesGrouperOpenSides;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A grouper compares the entities on its sides instead of the blocks when both sides are "open", which its config
 * describes as air or liquid. Supplementaries 4.0.2 counts any side without a full face toward the grouper as open, so a
 * grouper between two chests, slabs, stairs, flowers or carpets scans for entities that aren't there and reads 0. This
 * makes open mean air or liquid, as described.
 */
@Mixin(GrouperBlock.class)
public abstract class GrouperBlockMixin {

    @Inject(method = "isOpenSide", at = @At("HEAD"), cancellable = true)
    private static void lifeguard$airOrLiquidOnly(BlockState state, BlockGetter level, BlockPos pos, Direction towardsGrouper,
                                                CallbackInfoReturnable<Boolean> cir) {
        if (SupplementariesGrouperOpenSides.ENTRY.active())
            cir.setReturnValue(state.isAir() || state.liquid());
    }
}
