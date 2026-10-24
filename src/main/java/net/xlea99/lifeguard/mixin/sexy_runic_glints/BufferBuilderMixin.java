package net.xlea99.lifeguard.mixin.sexy_runic_glints;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.xlea99.lifeguard.entry.sexy_runic_glints.LenientVertices;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@link LenientVertices}: just before a lenient buffer would refuse a vertex for missing data, fill the missing parts
 * with neutral defaults. The setters only write elements the vertex still lacks, so whatever the renderer sent stays.
 */
@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements LenientVertices {

    @Shadow
    private int elementsToFill;
    @Shadow
    private int vertices;

    @Unique
    private boolean lifeguard$lenient;

    @Override
    public void lifeguard$setLenient(boolean lenient) {
        lifeguard$lenient = lenient;
    }

    @Inject(method = "endLastVertex", at = @At("HEAD"))
    private void lifeguard$fillMissing(CallbackInfo ci) {
        if (!lifeguard$lenient || vertices == 0 || elementsToFill == 0)
            return;
        BufferBuilder self = (BufferBuilder) (Object) this;
        self.setColor(-1);
        self.setUv(0, 0);
        self.setOverlay(OverlayTexture.NO_OVERLAY);
        self.setLight(LightTexture.FULL_BRIGHT);
        self.setNormal(0, 1, 0);
    }
}
