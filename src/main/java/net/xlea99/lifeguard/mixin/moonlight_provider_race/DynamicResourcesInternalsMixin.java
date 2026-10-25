package net.xlea99.lifeguard.mixin.moonlight_provider_race;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcesProvider;
import net.mehvahdjukaar.moonlight.core.pack.DynamicResourcesInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Makes registering a Moonlight dynamic resource provider thread-safe.
 *
 * <p>Every Moonlight mod (Supplementaries, Amendments, Vertical Slabs Compat, Every Compat, ...) registers the provider
 * that generates its assets and data from its mod constructor, and NeoForge constructs mods in parallel. Moonlight keeps
 * them in a plain {@code HashMultimap}, so on the launch where two land at once one is lost, and that mod's generated
 * resources are missing for the whole launch: every vertical slab drawn as the purple and black missing texture, say.
 * Registrations now take turns. The list is only read later, on one thread, when resources load.
 */
@Mixin(DynamicResourcesInternals.class)
public abstract class DynamicResourcesInternalsMixin {

    @Unique
    private static final Object LIFEGUARD$LOCK = new Object();

    @WrapMethod(method = "registerProvider")
    private static void lifeguard$oneAtATime(DynamicResourcesProvider provider, Operation<Void> original) {
        synchronized (LIFEGUARD$LOCK) {
            original.call(provider);
        }
    }
}
