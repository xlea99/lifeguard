package net.xlea99.lifeguard.mixin.scholar_supps_book_piles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.BookPileBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.scholar_supps_book_piles.PileCovers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A book in a book pile that Scholar colored gets its cover in that color: right after Supplementaries draws the book,
 * our cover overlay is drawn over it, tinted with the book's color (see {@link PileCovers}). Books with antique ink keep
 * their tattered look.
 *
 * <p>Only the pile's baked model gets the overlay (its quad builder carries the tint); the same method also draws a
 * pile's enchantment glint each frame, which has nothing to do with covers. Moonlight's builder bakes a quad only when
 * the next one starts, and reads the tint then, so the book's last quad is flushed (close: the builder carries on)
 * before the tint is set, or it would take the cover's color too.
 */
@Mixin(BookPileBlockTileRenderer.class)
public abstract class BookPileBlockTileRendererMixin {
    @Inject(method = "renderBook(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/function/Function;IILnet/mehvahdjukaar/supplementaries/common/block/tiles/BookPileBlockTile$BookVisualData;FF)V",
        at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/resources/model/BakedModel;FFFII)V"))
    private static void lifeguard$coloredCover(PoseStack poseStack, Function<BookPileBlockTile.BookVisualData, VertexConsumer> vertexBuilder,
                                               int light, int overlay, BookPileBlockTile.BookVisualData book, float xRot, float zRot,
                                               CallbackInfo ci) {
        ItemStack stack = ((BookVisualDataAccessor) book).lifeguard$getStack();
        ModelResourceLocation cover = PileCovers.coverModel(stack);
        if (cover == null || AntiqueInkItem.hasAntiqueInk(stack))
            return;
        ModelManager models = Minecraft.getInstance().getModelManager();
        BakedModel model = models.getModel(cover);
        if (model == models.getMissingModel() || !(vertexBuilder.apply(book) instanceof BakedQuadBuilder quads))
            return;
        try {
            quads.close();
        } catch (Exception e) {
            return;
        }
        quads.setTint(PileCovers.tint(stack));
        Minecraft.getInstance().getBlockRenderer().getModelRenderer()
            .renderModel(poseStack.last(), quads, null, model, 1.0F, 1.0F, 1.0F, light, overlay);
    }
}
