package net.xlea99.lifeguard.mixin.scholar_lectern_atlases;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.scholar.client.ColoredBookModel;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.scholar_lectern_atlases.AtlasCovers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Scholar's lectern drawing leaves atlases to their own covers ({@link AtlasCovers}). */
@Mixin(ColoredBookModel.class)
public abstract class ColoredBookModelMixin {

    @Inject(method = "renderOnLectern", at = @At("HEAD"), cancellable = true)
    private static void lifeguard$atlasCovers(BlockState state, LecternBlockEntity lectern, float partialTick,
                                              PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                              int packedOverlay, BookModel bookModel, CallbackInfo ci) {
        if (AtlasCovers.render(state, lectern, poseStack, buffer, packedLight, packedOverlay, bookModel))
            ci.cancel();
    }
}
