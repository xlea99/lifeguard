package net.xlea99.lifeguard.mixin.easy_shulker_boxes_dye_depot;

import com.google.gson.JsonElement;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.entry.easy_shulker_boxes_dye_depot.EasyShulkerBoxesDyeDepot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Easy Shulker Boxes' provider loader (its bundled Item Interactions) reads every file as is, with no load conditions,
 * so while the entry is off its Dye Depot providers are dropped here as the loader builds its providers.
 */
@Mixin(targets = "fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders")
public abstract class ItemContentsProvidersMixin {
    @ModifyVariable(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At("HEAD"), argsOnly = true)
    private Map<ResourceLocation, JsonElement> lifeguard$dyeDepotBoxesFollowTheSwitch(Map<ResourceLocation, JsonElement> providers) {
        if (EasyShulkerBoxesDyeDepot.ENTRY.active())
            return providers;
        String ours = EasyShulkerBoxesDyeDepot.ENTRY.id() + "/";
        return providers.entrySet().stream()
            .filter(entry -> !(entry.getKey().getNamespace().equals(Lifeguard.MODID) && entry.getKey().getPath().startsWith(ours)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
