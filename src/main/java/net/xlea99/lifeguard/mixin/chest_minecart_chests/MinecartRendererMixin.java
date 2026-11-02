package net.xlea99.lifeguard.mixin.chest_minecart_chests;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.chest_minecart_chests.ClientCartChests;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A chest minecart draws the chest it carries. See {@link ClientCartChests}. */
@Mixin(MinecartRenderer.class)
public abstract class MinecartRendererMixin {
    @WrapOperation(method = "render(Lnet/minecraft/world/entity/vehicle/AbstractMinecart;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;getDisplayBlockState()Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState lifeguard$showsItsChest(AbstractMinecart cart, Operation<BlockState> original) {
        return ClientCartChests.shown(cart, original.call(cart));
    }
}
