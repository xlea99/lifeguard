package net.xlea99.lifeguard.mixin.vanilla_backport_resin_trim_icons;

import com.mojang.datafixers.util.Pair;
import com.teamabnormals.blueprint.core.api.BlueprintTrims;
import java.util.LinkedHashMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Resin gets Blueprint's generated trim icons on every armor.
 *
 * <p>Vanilla Backport's resin shares copper's {@code item_model_index} (0.5), so no armor model's {@code trim_type}
 * overrides can tell them apart: a resin trim's icon is copper's. Blueprint generates icon overrides on every trimmable
 * armor under its own predicate, checked first, for the materials in its {@code generates_overrides} tag, which resin
 * isn't in. This adds resin to Blueprint's list on the client, before each pass that builds the overrides (at login and
 * after a resource reload), and runs the login pass itself when no tagged material started one. The sprites those
 * overrides point at come from this entry's pack (Vanilla Backport only adds resin to worn trims).
 */
@Mixin(BlueprintTrims.class)
public abstract class BlueprintTrimsMixin {
    private static final ResourceKey<TrimMaterial> RESIN =
        ResourceKey.create(Registries.TRIM_MATERIAL, ResourceLocation.withDefaultNamespace("resin"));

    @Shadow
    @Final
    private static LinkedHashMap<ResourceKey<TrimMaterial>, Pair<TrimMaterial, Float>> GENERATED_OVERRIDE_INDICES;

    @Shadow
    private static void modifyTrimmableItemModels(RegistryAccess registryAccess) {
        throw new AssertionError();
    }

    @Inject(method = "modifyTrimmableItemModels", at = @At("HEAD"))
    private static void lifeguard$addResin(RegistryAccess registryAccess, CallbackInfo ci) {
        if (GENERATED_OVERRIDE_INDICES.containsKey(RESIN))
            return;
        registryAccess.registryOrThrow(Registries.TRIM_MATERIAL).getHolder(RESIN).ifPresent(resin ->
            GENERATED_OVERRIDE_INDICES.put(RESIN, Pair.of(resin.value(), (float) GENERATED_OVERRIDE_INDICES.size())));
    }

    /** Blueprint only builds overrides at login when its tag has materials; without any, resin still needs the pass. */
    @Inject(method = "onClientLoggingIntoServer", at = @At("RETURN"))
    private static void lifeguard$resinAlone(ClientPlayerNetworkEvent.LoggingIn event, CallbackInfo ci) {
        if (GENERATED_OVERRIDE_INDICES.isEmpty())
            modifyTrimmableItemModels(event.getPlayer().clientLevel.registryAccess());
    }
}
