package net.xlea99.lifeguard.mixin.chest_boat_chests_quark;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.xlea99.lifeguard.entry.chest_boat_chests.ChestBoatChests;
import net.xlea99.lifeguard.entry.chest_boat_chests.ChestBoatTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.base.client.render.QuarkBoatRenderer;

/**
 * Quark draws its woods' boats itself, so its chest boats wear their chest here, where the texture goes into the
 * model's render type (the texture's own record is private). See {@link ChestBoatTextures}.
 */
@Mixin(QuarkBoatRenderer.class)
public abstract class QuarkBoatRendererMixin {
    @WrapOperation(method = "render(Lnet/minecraft/world/entity/vehicle/Boat;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/BoatModel;renderType(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType lifeguard$wearsItsChest(BoatModel model, ResourceLocation texture, Operation<RenderType> original,
                                             @Local(argsOnly = true) Boat boat) {
        return original.call(model, ChestBoatChests.QUARK.active() ? ChestBoatTextures.swap(boat, texture) : texture);
    }
}
