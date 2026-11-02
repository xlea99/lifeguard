package net.xlea99.lifeguard.mixin.pack_animal_chests;

import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.xlea99.lifeguard.entry.pack_animal_chests.PackAnimalChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A pack animal's chest came or went: players seeing it learn Quark's record of which. See {@link PackAnimalChest}. */
@Mixin(AbstractChestedHorse.class)
public abstract class AbstractChestedHorseMixin {
    @Inject(method = "setChest(Z)V", at = @At("TAIL"))
    private void lifeguard$syncsQuarkChest(boolean chested, CallbackInfo ci) {
        PackAnimalChest.changed((AbstractChestedHorse) (Object) this);
    }
}
