package net.xlea99.lifeguard.mixin.supps_moved_block_entities;

import net.mehvahdjukaar.supplementaries.common.misc.block_movement.BlockMovementHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A block a Supplementaries rope moves is told its contents changed once they arrive, as Quark's pistons do. The rope
 * places the block before its contents, so a block whose state follows them was left guessing: an Amendments cauldron
 * of lava, powder snow or acid lowered onto a campfire was taken for water and boiled until the block under it changed.
 */
@Mixin(BlockMovementHelper.class)
public abstract class BlockMovementHelperMixin {

    @Inject(method = "restoreBlockEntity", at = @At("TAIL"))
    private static void lifeguard$contentsChanged(Level level, BlockPos pos, BlockState placedState, CompoundTag nbt,
                                                  CallbackInfo ci) {
        BlockEntity moved = level.getBlockEntity(pos);
        if (!level.isClientSide && moved != null)
            moved.setChanged();
    }
}
