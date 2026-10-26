package net.xlea99.lifeguard.mixin.emf_haunted_harvest_masks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.hauntedharvest.client.HalloweenMaskLayer;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Haunted Harvest's trick-or-treater masks stay on their heads with resource packs that remodel villagers through Entity
 * Model Features (Fresh Animations).
 *
 * <p>The mask is the villager model's {@code head/hat} part, baked from the villager's model layer and drawn with the
 * villager's head pose copied onto it. Entity Model Features replaces that layer with a resource pack's model (Fresh
 * Animations moves the head into {@code headwear}/{@code nose}, with its hat cube 24px below the part's pivot and a brim
 * not named {@code hat_rim}), so the mask came out a block and a half off the head, with a stray brim. Baking vanilla's
 * villager mesh directly (Entity Model Features only swaps layers) gives the mask vanilla's hat again; the pose copy
 * still follows the real head, scale included (Fresh Animations' bigger baby heads).
 */
@Mixin(HalloweenMaskLayer.class)
public abstract class HalloweenMaskLayerMixin {

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;bakeLayer(Lnet/minecraft/client/model/geom/ModelLayerLocation;)Lnet/minecraft/client/model/geom/ModelPart;"))
    private ModelPart lifeguard$vanillaVillagerMesh(EntityRendererProvider.Context context, ModelLayerLocation layer,
                                                    Operation<ModelPart> original) {
        return LayerDefinition.create(VillagerModel.createBodyModel(), 64, 64).bakeRoot();
    }
}
