package net.xlea99.lifeguard.mixin.pack_animal_chests_alexs_mobs;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.xlea99.lifeguard.entry.pack_animal_chests.ElephantChests;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Which living entity is being drawn, for the elephant's chest layer, whose own render method names Alex's class (so a
 * hook there couldn't take the elephant without compiling Alex's Mobs in). See {@link ElephantChests}.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @WrapMethod(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void lifeguard$notesDrawnEntity(LivingEntity entity, float yaw, float partialTicks, PoseStack poseStack,
                                            MultiBufferSource buffer, int light, Operation<Void> original) {
        Entity previous = ElephantChests.startDrawing(entity);
        try {
            original.call(entity, yaw, partialTicks, poseStack, buffer, light);
        } finally {
            ElephantChests.endDrawing(previous);
        }
    }
}
