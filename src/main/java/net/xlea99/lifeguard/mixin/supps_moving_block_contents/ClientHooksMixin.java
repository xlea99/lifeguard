package net.xlea99.lifeguard.mixin.supps_moving_block_contents;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.model.data.ModelData;
import com.llamalad7.mixinextras.sugar.Local;
import net.xlea99.lifeguard.entry.supps_moving_block_contents.MovingContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * NeoForge draws a moving block with no model data and the world as it is, where the moving block stands in for its
 * contents. For a block whose contents travel with it ({@link PistonHeadRendererMixin}), the model gets the contents'
 * data and a world where they're in place, so it draws them and colors them as it does standing still.
 */
@Mixin(ClientHooks.class)
public abstract class ClientHooksMixin {

    @WrapOperation(method = "renderPistonMovedBlocks",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/resources/model/BakedModel;getRenderTypes(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;)Lnet/neoforged/neoforge/client/ChunkRenderTypeSet;"))
    private static ChunkRenderTypeSet lifeguard$contentsRenderTypes(BakedModel model, BlockState state, RandomSource random,
                                                                    ModelData data, Operation<ChunkRenderTypeSet> original,
                                                                    @Local(argsOnly = true) BlockPos pos,
                                                                    @Local(argsOnly = true) net.minecraft.world.level.Level level) {
        BlockEntity contents = MovingContents.at(pos, state);
        if (contents != null)
            data = model.getModelData(MovingContents.withContents(level, pos, contents), pos, state, contents.getModelData());
        return original.call(model, state, random, data);
    }

    @WrapOperation(method = "renderPistonMovedBlocks",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;tesselateBlock(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V"))
    private static void lifeguard$contentsModel(ModelBlockRenderer renderer, BlockAndTintGetter level, BakedModel model,
                                                BlockState state, BlockPos pos, PoseStack pose, VertexConsumer consumer,
                                                boolean checkSides, RandomSource random, long seed, int overlay,
                                                ModelData data, RenderType type, Operation<Void> original) {
        BlockEntity contents = MovingContents.at(pos, state);
        if (contents != null) {
            level = MovingContents.withContents(level, pos, contents);
            data = model.getModelData(level, pos, state, contents.getModelData());
        }
        original.call(renderer, level, model, state, pos, pose, consumer, checkSides, random, seed, overlay, data, type);
    }
}
