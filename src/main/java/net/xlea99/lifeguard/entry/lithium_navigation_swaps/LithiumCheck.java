package net.xlea99.lifeguard.entry.lithium_navigation_swaps;

import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.xlea99.lifeguard.boot.BrokenEntries;

/**
 * Checks, by name only, that Lithium still has the internal interface {@link NavigationSwaps} calls. A Lithium update
 * that renames it breaks this entry (off, loudly) at startup instead of crashing on the first mob that paths. Kept apart
 * from {@link NavigationSwaps} so this class never links against Lithium's types itself.
 */
final class LithiumCheck {
    static final String INTERFACE = "net.caffeinemc.mods.lithium.common.entity.NavigatingEntity";

    private LithiumCheck() {
    }

    static void run() {
        try {
            Class<?> navigating = Class.forName(INTERFACE);
            navigating.getMethod("lithium$isRegisteredToWorld");
            navigating.getMethod("lithium$getRegisteredNavigation");
            navigating.getMethod("lithium$updateNavigationRegistration");
            if (!PathNavigation.class.isAssignableFrom(navigating.getMethod("lithium$getRegisteredNavigation").getReturnType()))
                throw new NoSuchMethodException("lithium$getRegisteredNavigation no longer returns a PathNavigation");
        } catch (ReflectiveOperationException | LinkageError e) {
            BrokenEntries.mark(LithiumNavigationSwaps.ENTRY, "Lithium's " + INTERFACE + " changed (" + e
                + "), so mobs' navigation records can't be kept in sync");
        }
    }
}
