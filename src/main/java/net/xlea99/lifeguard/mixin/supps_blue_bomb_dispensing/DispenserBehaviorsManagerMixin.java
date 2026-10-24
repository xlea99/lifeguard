package net.xlea99.lifeguard.mixin.supps_blue_bomb_dispensing;

import net.mehvahdjukaar.supplementaries.common.block.dispenser.DispenserBehaviorsManager;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dispensers throw blue bombs again, as in Supplementaries 3.1 (and as they still throw plain bombs). 4.0.2 gives the
 * blue bomb Moonlight's "add to the container in front" behavior, which falls back to whatever the dispenser did with
 * the item before; nothing was registered, so with no container in front the bomb just drops. This registers the throw
 * right before, so it becomes that fallback, and loading a container in front still works.
 */
@Mixin(DispenserBehaviorsManager.class)
public abstract class DispenserBehaviorsManagerMixin {

    // The second "add to inventory" behavior made there is the blue bomb's (the first is the cookie's).
    @Inject(method = "registerBehaviors",
        at = @At(value = "NEW", target = "net/mehvahdjukaar/moonlight/api/util/DispenserHelper$AddItemToInventoryBehavior", ordinal = 1))
    private static void lifeguard$throwUnderneath(CallbackInfo ci) {
        DispenserBlock.registerBehavior(ModRegistry.BOMB_BLUE_ITEM.get(), new ProjectileDispenseBehavior(ModRegistry.BOMB_BLUE_ITEM.get()));
    }
}
