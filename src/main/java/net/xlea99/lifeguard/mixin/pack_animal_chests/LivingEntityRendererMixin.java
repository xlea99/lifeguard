package net.xlea99.lifeguard.mixin.pack_animal_chests;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.xlea99.lifeguard.entry.pack_animal_chests.PackAnimalTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Donkeys, mules, llamas and trader llamas wear the chest they were given: their skin, whoever picked it, gets its bags
 * recolored where the render type is made from it. Hooked here, not at the renderers' getTextureLocation: Quark's shiny
 * llamas return their skin from the head of LlamaRenderer's, skipping a hook at its return. See {@link PackAnimalTextures}.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @WrapOperation(method = "getRenderType(Lnet/minecraft/world/entity/LivingEntity;ZZZ)Lnet/minecraft/client/renderer/RenderType;",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getTextureLocation(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation lifeguard$wearsItsChest(LivingEntityRenderer<?, ?> renderer, Entity entity, Operation<ResourceLocation> original) {
        return PackAnimalTextures.swap(renderer, entity, original.call(renderer, entity));
    }
}
