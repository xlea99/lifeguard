package net.xlea99.lifeguard.mixin.moonlight_registration_races;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Makes Moonlight's list of the mods that depend on it thread-safe.
 *
 * <p>A mod joins the list the first time it registers anything through Moonlight, from its mod constructor, and NeoForge
 * constructs mods in parallel. It's a plain {@code HashSet}, so two joining at once can lose one, and for that launch
 * Moonlight skips that mod's display blocks (pedestals, shelves, ...) when it gives them item handlers for hoppers.
 * Additions now take turns. The list is only read later, on one thread.
 */
@Mixin(Moonlight.class)
public abstract class MoonlightMixin {

    @Unique
    private static final Object LIFEGUARD$LOCK = new Object();

    @WrapMethod(method = "addDependent")
    private static void lifeguard$oneAtATime(String modId, Operation<Void> original) {
        synchronized (LIFEGUARD$LOCK) {
            original.call(modId);
        }
    }
}
