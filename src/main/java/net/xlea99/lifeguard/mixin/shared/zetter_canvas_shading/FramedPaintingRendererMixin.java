package net.xlea99.lifeguard.mixin.shared.zetter_canvas_shading;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import me.dantaeusb.zetter.client.renderer.CanvasRenderer;
import me.dantaeusb.zetter.client.renderer.entity.FramedPaintingRenderer;
import me.dantaeusb.zetter.storage.AbstractCanvasData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.xlea99.lifeguard.entry.zettify_all_paintings.VirtualPaintingShading;
import net.xlea99.lifeguard.entry.zettify_all_paintings.VirtualPaintings;
import net.xlea99.lifeguard.entry.zettify_all_paintings.ZettifyAllPaintings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Marks a hung canvas draw that gets vanilla's painting lighting: a virtual painting's ({@link ZettifyAllPaintings#SHADING})
 * or every painting's ({@link ZettifyAllPaintings#SHADING_ALL}). Shared group {@code zetter_canvas_shading}.
 */
@Mixin(FramedPaintingRenderer.class)
public abstract class FramedPaintingRendererMixin {
    @WrapOperation(method = "render(Lme/dantaeusb/zetter/entity/item/PaintingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lme/dantaeusb/zetter/client/renderer/CanvasRenderer;renderCanvas(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Ljava/lang/String;Lme/dantaeusb/zetter/storage/AbstractCanvasData;I)V"))
    private void lifeguard$markHungVirtual(CanvasRenderer renderer, PoseStack pose, MultiBufferSource buffers, String code,
                                           AbstractCanvasData canvas, int light, Operation<Void> original) {
        boolean shade = ZettifyAllPaintings.SHADING_ALL.active()
            || (VirtualPaintings.isVirtual(code) && ZettifyAllPaintings.SHADING.active());
        VirtualPaintingShading.shadeThisCanvas = shade;
        try {
            original.call(renderer, pose, buffers, code, canvas, light);
        } finally {
            VirtualPaintingShading.shadeThisCanvas = false;
        }
    }
}
