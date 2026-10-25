package net.xlea99.lifeguard.mixin.whiter_labels;

import net.mehvahdjukaar.labels.LabelEntityRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.xlea99.lifeguard.entry.whiter_labels.WhiterPaper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** A placed label's paper is drawn with our whiter model ({@link WhiterPaper}). */
@Mixin(LabelEntityRenderer.class)
public abstract class LabelEntityRendererMixin {

    @ModifyArg(method = "render", index = 1, at = @At(value = "INVOKE",
        target = "Lnet/mehvahdjukaar/moonlight/api/platform/ClientHelper;getModel(Lnet/minecraft/client/resources/model/ModelManager;Lnet/minecraft/client/resources/model/ModelResourceLocation;)Lnet/minecraft/client/resources/model/BakedModel;"))
    private ModelResourceLocation lifeguard$whiterPaper(ModelResourceLocation model) {
        return WhiterPaper.MODEL;
    }
}
