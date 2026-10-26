package net.xlea99.lifeguard.mixin.haunted_harvest_torch_lanterns;

import net.mehvahdjukaar.hauntedharvest.integration.CompatHandler;
import net.mehvahdjukaar.hauntedharvest.integration.platform.AutumnityCompatImpl;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.entry.haunted_harvest_torch_lanterns.TorchLanterns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Without Autumnity, the torch lanterns' types are filed under Haunted Harvest's namespace ({@link TorchLanterns}):
 * Moonlight registers each id on its namespace's mod bus, and there is no Autumnity bus. The path keeps the
 * {@code autumnity/} folder, which is where Haunted Harvest keeps these lanterns' textures (it takes the texture folder
 * from the id).
 */
@Mixin(AutumnityCompatImpl.class)
public abstract class AutumnityCompatImplMixin {

    @Inject(method = "res", at = @At("HEAD"), cancellable = true)
    private static void lifeguard$hauntedHarvestNamespace(String name, CallbackInfoReturnable<ResourceLocation> cir) {
        if (!CompatHandler.AUTUMNITY_INSTALLED)
            cir.setReturnValue(ResourceLocation.fromNamespaceAndPath("hauntedharvest", TorchLanterns.FOLDER + name));
    }
}
