package net.xlea99.lifeguard.mixin.haunted_harvest_torch_lanterns;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.hauntedharvest.blocks.PumpkinType;
import net.xlea99.lifeguard.entry.haunted_harvest_torch_lanterns.HauntedHarvestTorchLanterns;
import net.xlea99.lifeguard.entry.haunted_harvest_torch_lanterns.TorchLanterns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** With the entry off, the unlocked lanterns' torches light nothing ({@link TorchLanterns}); placed ones stay. */
@Mixin(PumpkinType.class)
public abstract class PumpkinTypeMixin {

    @ModifyReturnValue(method = "getFromTorch", at = @At("RETURN"))
    private static PumpkinType lifeguard$onlyWhileOn(PumpkinType type) {
        return type != null && !HauntedHarvestTorchLanterns.ENTRY.active() && TorchLanterns.unlockedHere(type) ? null : type;
    }
}
