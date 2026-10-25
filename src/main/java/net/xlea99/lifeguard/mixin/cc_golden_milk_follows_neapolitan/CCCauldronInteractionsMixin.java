package net.xlea99.lifeguard.mixin.cc_golden_milk_follows_neapolitan;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.Lifeguard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A Caverns & Chasms golden milk bucket only fills a cauldron with Neapolitan's milk cauldron while Neapolitan's own
 * "Milk Cauldron" setting is on. Caverns & Chasms checked only that the block exists (it always does with Neapolitan
 * installed), so with the setting off a golden bucket still made the milk cauldron a regular milk bucket can't. Reads
 * Neapolitan's setting by name.
 */
@Mixin(targets = "com.teamabnormals.caverns_and_chasms.core.other.CCCauldronInteractions")
public abstract class CCCauldronInteractionsMixin {

    @WrapOperation(method = "registerCauldronInteractions",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/DefaultedRegistry;containsKey(Lnet/minecraft/resources/ResourceLocation;)Z"))
    private static boolean lifeguard$onlyWithNeapolitansMilkCauldron(DefaultedRegistry<?> registry, ResourceLocation id,
                                                                     Operation<Boolean> original) {
        return original.call(registry, id) && neapolitanMilkCauldronOn();
    }

    @Unique
    private static boolean neapolitanMilkCauldronOn() {
        try {
            Class<?> config = Class.forName("com.teamabnormals.neapolitan.core.NeapolitanConfig");
            Object common = config.getField("COMMON").get(null);
            Object value = common.getClass().getField("milkCauldron").get(common);
            return !Boolean.FALSE.equals(value.getClass().getMethod("get").invoke(value));
        } catch (ReflectiveOperationException | RuntimeException e) {
            Lifeguard.LOGGER.warn("Lifeguard: couldn't read Neapolitan's Milk Cauldron setting; leaving Caverns & Chasms' golden milk bucket as it is", e);
            return true;
        }
    }
}
