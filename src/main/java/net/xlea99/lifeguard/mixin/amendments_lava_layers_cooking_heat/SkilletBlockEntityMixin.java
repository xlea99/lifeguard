package net.xlea99.lifeguard.mixin.amendments_lava_layers_cooking_heat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.amendments_lava_layers.CookingHeat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;

/** A placed skillet over a cauldron of lava is heated (see {@link CookingHeat}). */
@Mixin(SkilletBlockEntity.class)
public abstract class SkilletBlockEntityMixin {

    @WrapOperation(method = {"cookingTick", "animationTick"},
        at = @At(value = "INVOKE",
            target = "Lvectorwing/farmersdelight/common/block/entity/SkilletBlockEntity;isHeated(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
    private static boolean lifeguard$ticksHeatedByLava(SkilletBlockEntity skillet, Level level, BlockPos pos,
                                                       Operation<Boolean> original) {
        return original.call(skillet, level, pos) || CookingHeat.heatsFromBelow(skillet, level, pos);
    }

    @WrapOperation(method = {"isHeated()Z", "addItemToCook"},
        at = @At(value = "INVOKE",
            target = "Lvectorwing/farmersdelight/common/block/entity/SkilletBlockEntity;isHeated(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean lifeguard$heatedByLava(SkilletBlockEntity skillet, Level level, BlockPos pos,
                                           Operation<Boolean> original) {
        return original.call(skillet, level, pos) || CookingHeat.heatsFromBelow(skillet, level, pos);
    }
}
