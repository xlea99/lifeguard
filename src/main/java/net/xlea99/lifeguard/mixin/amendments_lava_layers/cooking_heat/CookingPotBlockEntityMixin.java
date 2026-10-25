package net.xlea99.lifeguard.mixin.amendments_lava_layers.cooking_heat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.amendments_lava_layers.CookingHeat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

/** A cooking pot over a cauldron of lava is heated (see {@link CookingHeat}). */
@Mixin(CookingPotBlockEntity.class)
public abstract class CookingPotBlockEntityMixin {

    @WrapOperation(method = {"cookingTick", "animationTick"},
        at = @At(value = "INVOKE",
            target = "Lvectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity;isHeated(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
    private static boolean lifeguard$ticksHeatedByLava(CookingPotBlockEntity pot, Level level, BlockPos pos,
                                                       Operation<Boolean> original) {
        return original.call(pot, level, pos) || CookingHeat.heatsFromBelow(pot, level, pos);
    }

    @WrapOperation(method = "isHeated()Z",
        at = @At(value = "INVOKE",
            target = "Lvectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity;isHeated(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean lifeguard$heatedByLava(CookingPotBlockEntity pot, Level level, BlockPos pos, Operation<Boolean> original) {
        return original.call(pot, level, pos) || CookingHeat.heatsFromBelow(pot, level, pos);
    }
}
