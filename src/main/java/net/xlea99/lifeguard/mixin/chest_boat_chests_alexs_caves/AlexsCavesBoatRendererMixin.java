package net.xlea99.lifeguard.mixin.chest_boat_chests_alexs_caves;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.xlea99.lifeguard.entry.chest_boat_chests.ChestBoatChests;
import net.xlea99.lifeguard.entry.chest_boat_chests.ChestBoatTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Alex's Caves draws a chest boat's chest as its own model, from its own chest texture, in the first render type it
 * asks for (the boat's own comes second): the chest it carries is drawn onto that texture. See {@link ChestBoatTextures}.
 */
@Mixin(targets = "com.github.alexmodguy.alexscaves.client.render.entity.AlexsCavesBoatRenderer")
public abstract class AlexsCavesBoatRendererMixin {
    @WrapOperation(method = "render(Lnet/minecraft/world/entity/vehicle/Boat;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/renderer/RenderType;entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType lifeguard$wearsItsChest(ResourceLocation chestTexture, Operation<RenderType> original,
                                             @Local(argsOnly = true) Boat boat) {
        return original.call(ChestBoatChests.ALEXS_CAVES.active() ? ChestBoatTextures.swapAlexsCavesChest(boat, chestTexture) : chestTexture);
    }
}
