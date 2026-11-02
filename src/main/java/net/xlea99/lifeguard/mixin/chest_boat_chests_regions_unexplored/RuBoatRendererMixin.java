package net.xlea99.lifeguard.mixin.chest_boat_chests_regions_unexplored;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.xlea99.lifeguard.entry.chest_boat_chests.ChestBoatChests;
import net.xlea99.lifeguard.entry.chest_boat_chests.ChestBoatTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Regions Unexplored's renderer draws its boats itself, taking the (texture, model) pair from its own map, so its chest
 * boats wear their chest here, where it reads the texture out of that pair. See {@link ChestBoatTextures}.
 */
@Mixin(targets = "net.regions_unexplored.entity.client.RuBoatRenderer")
public abstract class RuBoatRendererMixin {
    @WrapOperation(method = "render(Lnet/minecraft/world/entity/vehicle/Boat;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Pair;getFirst()Ljava/lang/Object;"))
    private Object lifeguard$wearsItsChest(Pair<?, ?> pair, Operation<Object> original, @Local(argsOnly = true) Boat boat) {
        Object texture = original.call(pair);
        return ChestBoatChests.REGIONS_UNEXPLORED.active() && texture instanceof ResourceLocation location
            ? ChestBoatTextures.swap(boat, location) : texture;
    }
}
