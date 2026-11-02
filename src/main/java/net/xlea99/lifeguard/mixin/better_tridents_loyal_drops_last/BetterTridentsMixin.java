package net.xlea99.lifeguard.mixin.better_tridents_loyal_drops_last;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fuzs.puzzleslib.api.event.v1.core.EventInvoker;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.entity.living.LivingDropsCallback;
import net.xlea99.lifeguard.entry.better_tridents_loyal_drops_last.BetterTridentsLoyalDropsLast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Better Tridents' loyalty drops listener registers last instead of at the default phase. Reached by class name (no
 * Better Tridents in the build). See {@link BetterTridentsLoyalDropsLast}.
 */
@Mixin(targets = "fuzs.bettertridents.BetterTridents")
public abstract class BetterTridentsMixin {
    @SuppressWarnings("unchecked")
    @WrapOperation(method = "registerEventHandlers", at = @At(value = "INVOKE",
        target = "Lfuzs/puzzleslib/api/event/v1/core/EventInvoker;register(Ljava/lang/Object;)V", ordinal = 0))
    private static void lifeguard$loyalDropsLast(EventInvoker<?> invoker, Object callback, Operation<Void> original) {
        // The first register call is LivingDropsCallback's (the loyalty drops); anything else is left alone.
        if (invoker == LivingDropsCallback.EVENT)
            ((EventInvoker<Object>) invoker).register(EventPhase.LAST, callback);
        else
            original.call(invoker, callback);
    }
}
