package net.xlea99.lifeguard.mixin.haunted_harvest_pumpkin_compat;

import java.util.function.Predicate;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.haunted_harvest_pumpkin_compat.Pumpkins;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Golems' heads can be Haunted Harvest's pumpkins ({@link Pumpkins}): vanilla's iron golem shape and every golem shape
 * built on vanilla's (Vanilla Backport's copper golem) take only vanilla's two pumpkins. Haunted Harvest builds its iron
 * golems with vanilla's shape, so none ever formed; its snow golems have their own check.
 */
@Mixin(CarvedPumpkinBlock.class)
public abstract class CarvedPumpkinBlockMixin {
    @Shadow
    @Final
    @Mutable
    private static Predicate<BlockState> PUMPKINS_PREDICATE;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void lifeguard$hauntedPumpkinsToo(CallbackInfo ci) {
        PUMPKINS_PREDICATE = PUMPKINS_PREDICATE.or(state -> state != null && Pumpkins.isPumpkin(state.getBlock()));
    }
}
