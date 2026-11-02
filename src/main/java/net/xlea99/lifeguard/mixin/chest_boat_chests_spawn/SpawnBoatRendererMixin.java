package net.xlea99.lifeguard.mixin.chest_boat_chests_spawn;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import com.ninni.spawn.client.renderer.entity.misc.SpawnBoatRenderer;
import com.ninni.spawn.server.entity.misc.SpawnBoat;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.entry.chest_boat_chests.ChestBoatChests;
import net.xlea99.lifeguard.entry.chest_boat_chests.ChestBoatTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Spawn draws its boats itself, taking the (texture, model) pair from its own map, so its chest boats wear their chest
 * here, where it reads the texture out of that pair. See {@link ChestBoatTextures}.
 */
@Mixin(SpawnBoatRenderer.class)
public abstract class SpawnBoatRendererMixin {
    @WrapOperation(method = "render(Lcom/ninni/spawn/server/entity/misc/SpawnBoat;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Pair;getFirst()Ljava/lang/Object;"))
    private Object lifeguard$wearsItsChest(Pair<?, ?> pair, Operation<Object> original, @Local(argsOnly = true) SpawnBoat boat) {
        Object texture = original.call(pair);
        return ChestBoatChests.SPAWN.active() && texture instanceof ResourceLocation location
            ? ChestBoatTextures.swap(boat, location) : texture;
    }
}
