package net.xlea99.lifeguard.mixin.supps_moving_block_contents;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.ICarryingMovingPiston;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.llamalad7.mixinextras.sugar.Local;
import net.xlea99.lifeguard.entry.supps_moving_block_contents.MovingContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A block moved by a piston or pulley is drawn with its contents, when Supplementaries carries them along: an Amendments
 * cauldron keeps its liquid on the way. Its look comes from its contents (model data, colors), which the moving copy
 * never had; Supplementaries already rebuilds the contents to draw moving chests, so they're lent to the drawing
 * ({@link ClientHooksMixin}).
 */
@Mixin(PistonHeadRenderer.class)
public abstract class PistonHeadRendererMixin {

    @WrapOperation(method = "render",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/blockentity/PistonHeadRenderer;renderBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;ZI)V"))
    private void lifeguard$withContents(PistonHeadRenderer self, BlockPos pos, BlockState state, PoseStack pose,
                                        MultiBufferSource buffers, Level level, boolean extended, int overlay,
                                        Operation<Void> original,
                                        @Local(argsOnly = true) PistonMovingBlockEntity moving) {
        BlockEntity contents = moving instanceof ICarryingMovingPiston carrying && carrying.supp$getCarriedBlockEntityNbt() != null
            ? carrying.supp$getOrCreateCachedCarriedBlockEntity() : null;
        if (contents == null) {
            original.call(self, pos, state, pose, buffers, level, extended, overlay);
            return;
        }
        MovingContents.begin(contents, pos);
        try {
            original.call(self, pos, state, pose, buffers, level, extended, overlay);
        } finally {
            MovingContents.end();
        }
    }
}
