package net.xlea99.lifeguard.entry.scholar_lectern_atlases;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import pepjebs.mapatlases.client.MapAtlasesClient;
import pepjebs.mapatlases.utils.AtlasLectern;

/**
 * An atlas on a lectern keeps Map Atlases' covers under Scholar ({@link ScholarLecternAtlases},
 * {@code mixin.scholar_lectern_atlases}).
 *
 * <p>Scholar draws whatever lies on a lectern itself, as one of its books dyed to the book's color, and skips vanilla's
 * lectern drawing. Map Atlases gives an atlas its covers inside that vanilla drawing, so an atlas on a lectern showed as a
 * plain book. Here an atlas is drawn as vanilla and Map Atlases would; books are left to Scholar.
 */
public final class AtlasCovers {
    private AtlasCovers() {
    }

    /** Draws the lectern's atlas if it holds one; false (nothing drawn) for anything else. */
    public static boolean render(BlockState state, LecternBlockEntity lectern, PoseStack poseStack, MultiBufferSource buffer,
                                 int packedLight, int packedOverlay, BookModel bookModel) {
        if (!(lectern instanceof AtlasLectern atlasLectern) || !atlasLectern.mapatlases$hasAtlas())
            return false;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0625F, 0.5F);
        float facing = state.getValue(LecternBlock.FACING).getClockWise().toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing));
        poseStack.mulPose(Axis.ZP.rotationDegrees(67.5F));
        poseStack.translate(0.0F, -0.125F, 0.0F);
        bookModel.setupAnim(0.0F, 0.1F, 0.9F, 1.2F);
        bookModel.render(poseStack, cover(lectern.getLevel()).buffer(buffer, RenderType::entitySolid), packedLight, packedOverlay, -1);
        poseStack.popPose();
        return true;
    }

    /** The cover Map Atlases picks for the lectern's dimension. */
    private static Material cover(@Nullable Level level) {
        ResourceKey<Level> dimension = level == null ? null : level.dimension();
        if (dimension == Level.OVERWORLD)
            return MapAtlasesClient.OVERWORLD_TEXTURE;
        if (dimension == Level.NETHER)
            return MapAtlasesClient.NETHER_TEXTURE;
        if (dimension == Level.END)
            return MapAtlasesClient.END_TEXTURE;
        return MapAtlasesClient.OTHER_TEXTURE;
    }
}
