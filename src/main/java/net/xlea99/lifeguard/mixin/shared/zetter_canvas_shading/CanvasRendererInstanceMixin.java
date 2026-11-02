package net.xlea99.lifeguard.mixin.shared.zetter_canvas_shading;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.entry.zettify_all_paintings.VirtualPaintingShading;
import net.xlea99.lifeguard.entry.zettify_all_paintings.ZettifyAllPaintings;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Zetter's per-canvas texture holder. Keeps the texture location Zetter registers, and while a hung virtual painting is
 * drawn, draws the same quad as Zetter (same corners and UVs) but as vanilla PaintingRenderer draws a painting's front:
 * entitySolid, white, no overlay, normal (0, 0, -1) (the quad's visible side, as its winding and Zetter's culled text
 * render type show). See {@link ZettifyAllPaintings#SHADING}.
 */
@Mixin(targets = "me.dantaeusb.zetter.client.renderer.CanvasRenderer$Instance")
public abstract class CanvasRendererInstanceMixin {
    @Shadow
    @Final
    private int blockPixelWidth;

    @Shadow
    @Final
    private int blockPixelHeight;

    @Unique
    private ResourceLocation lifeguard$texture;

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/texture/TextureManager;register(Ljava/lang/String;Lnet/minecraft/client/renderer/texture/DynamicTexture;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation lifeguard$keepTexture(ResourceLocation location) {
        this.lifeguard$texture = location;
        return location;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void lifeguard$shadedHungVirtual(PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (!VirtualPaintingShading.shadeThisCanvas || this.lifeguard$texture == null)
            return;
        PoseStack.Pose last = pose.last();
        Matrix4f matrix = last.pose();
        VertexConsumer out = buffers.getBuffer(RenderType.entitySolid(this.lifeguard$texture));
        float w = this.blockPixelWidth;
        float h = this.blockPixelHeight;
        lifeguard$vertex(out, last, matrix, 0.0F, h, 0.0F, 1.0F, light);
        lifeguard$vertex(out, last, matrix, w, h, 1.0F, 1.0F, light);
        lifeguard$vertex(out, last, matrix, w, 0.0F, 1.0F, 0.0F, light);
        lifeguard$vertex(out, last, matrix, 0.0F, 0.0F, 0.0F, 0.0F, light);
        ci.cancel();
    }

    @Unique
    private static void lifeguard$vertex(VertexConsumer out, PoseStack.Pose last, Matrix4f matrix, float x, float y,
                                         float u, float v, int light) {
        out.addVertex(matrix, x, y, 0.0F).setColor(255, 255, 255, 255).setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(last, 0.0F, 0.0F, -1.0F);
    }
}
