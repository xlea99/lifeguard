package net.xlea99.lifeguard.mixin.chest_boat_chests;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.xlea99.lifeguard.entry.chest_boat_chests.ChestBoatTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A chest boat drawn by vanilla's renderer, or one extending it that only picks its own texture (Blueprint's, Vanilla
 * Backport's), wears the chest it carries. See {@link ChestBoatTextures}.
 */
@Mixin(BoatRenderer.class)
public abstract class BoatRendererMixin {
    @WrapOperation(method = "render(Lnet/minecraft/world/entity/vehicle/Boat;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/BoatRenderer;getModelWithLocation(Lnet/minecraft/world/entity/vehicle/Boat;)Lcom/mojang/datafixers/util/Pair;"))
    private Pair<ResourceLocation, ListModel<Boat>> lifeguard$wearsItsChest(BoatRenderer renderer, Boat boat,
                                                                          Operation<Pair<ResourceLocation, ListModel<Boat>>> original) {
        return ChestBoatTextures.swap(boat, original.call(renderer, boat));
    }
}
