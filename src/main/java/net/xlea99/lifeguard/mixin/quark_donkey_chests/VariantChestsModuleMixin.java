package net.xlea99.lifeguard.mixin.quark_donkey_chests;

import net.xlea99.lifeguard.entry.quark_donkey_chests.QuarkDonkeyChests;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.content.building.module.VariantChestsModule;
import org.violetmoon.zeta.event.play.entity.living.ZLivingDeath;

/**
 * Turns off Quark's own swap of a dying donkey's chest, which {@link AbstractChestedHorseMixin} does exactly instead.
 *
 * <p>When a donkey, mule or llama wearing a Quark chest dies, Quark turns the next plain chest item to enter the world
 * into that Quark chest. But vanilla drops the animal's bags before the chest it wears, so a stack of plain chests it
 * carried became one Quark chest (the rest were lost) and the worn chest dropped plain. And when the drops never came
 * (doMobLoot off, or another mod clearing them), the next plain chest dropped anywhere was swapped.
 */
@Mixin(VariantChestsModule.class)
public abstract class VariantChestsModuleMixin {

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void lifeguard$dropTheWornChestOurselves(ZLivingDeath event, CallbackInfo ci) {
        if (QuarkDonkeyChests.ENTRY.active())
            ci.cancel();
    }
}
