package net.xlea99.lifeguard.mixin.pack_animal_chests_alexs_mobs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.entry.pack_animal_chests.ElephantChests;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** An elephant's chest layer wears the chest it was given. See {@link ElephantChests}. */
@Mixin(targets = "com.github.alexthe666.alexsmobs.client.render.layer.LayerElephantOverlays")
public abstract class LayerElephantOverlaysMixin {
    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILcom/github/alexthe666/alexsmobs/entity/EntityElephant;FFFFFF)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderType;entityCutout(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType lifeguard$wearsItsChest(ResourceLocation texture, Operation<RenderType> original) {
        return original.call(ElephantChests.chestLayer(texture));
    }
}
