package net.xlea99.lifeguard.mixin.moonlight_registration_races;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.function.Consumer;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Makes adding a Moonlight dispenser listener thread-safe.
 *
 * <p>Every Moonlight mod (Supplementaries, Amendments, ...) adds the listener that registers its dispenser behaviors
 * from its mod constructor, and NeoForge constructs mods in parallel. Moonlight keeps them in plain {@code ArrayList}s,
 * so two landing at once either crash the launch ({@code ArrayIndexOutOfBoundsException} in {@code ArrayList.add}, seen
 * with Amendments 2.2.1) or quietly lose one, and that mod's dispenser behaviors are missing for the whole launch.
 * Additions now take turns. The lists are only read later, on one thread.
 */
@Mixin(DispenserHelper.class)
public abstract class DispenserHelperMixin {

    @Unique
    private static final Object LIFEGUARD$LOCK = new Object();

    @WrapMethod(method = "addListener")
    private static void lifeguard$oneAtATime(Consumer<DispenserHelper.Event> listener, DispenserHelper.Priority priority,
                                             Operation<Void> original) {
        synchronized (LIFEGUARD$LOCK) {
            original.call(listener, priority);
        }
    }
}
