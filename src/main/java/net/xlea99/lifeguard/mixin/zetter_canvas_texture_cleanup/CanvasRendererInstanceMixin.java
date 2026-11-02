package net.xlea99.lifeguard.mixin.zetter_canvas_texture_cleanup;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.entry.zetter_canvas_texture_cleanup.ZetterCanvasTextureCleanup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Closing a canvas instance also releases its TextureManager registration (TextureManager.release; closing the texture
 * twice is safe, DynamicTexture.close checks). See {@link ZetterCanvasTextureCleanup}.
 */
@Mixin(targets = "me.dantaeusb.zetter.client.renderer.CanvasRenderer$Instance")
public abstract class CanvasRendererInstanceMixin {
    @Unique
    private ResourceLocation lifeguard$registered;

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/texture/TextureManager;register(Ljava/lang/String;Lnet/minecraft/client/renderer/texture/DynamicTexture;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation lifeguard$remember(ResourceLocation location) {
        this.lifeguard$registered = location;
        return location;
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void lifeguard$release(CallbackInfo ci) {
        if (this.lifeguard$registered != null) {
            Minecraft.getInstance().getTextureManager().release(this.lifeguard$registered);
            this.lifeguard$registered = null;
        }
    }
}
