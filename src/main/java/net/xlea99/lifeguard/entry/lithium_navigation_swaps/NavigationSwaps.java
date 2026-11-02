package net.xlea99.lifeguard.entry.lithium_navigation_swaps;

import net.caffeinemc.mods.lithium.common.entity.NavigatingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

/**
 * Keeps Lithium's record of a mob's navigator on the mob's current one. Only called while the entry is active (so
 * Lithium is installed and {@link LithiumCheck} found its interface).
 */
public final class NavigationSwaps {
    private NavigationSwaps() {
    }

    /** Called as {@code navigation} starts a path. */
    public static void beforePath(Mob mob, PathNavigation navigation) {
        // An old, swapped-out navigator a mod still calls stays out of it: Lithium tracks the mob's current one.
        if (mob.level().isClientSide() || mob.getNavigation() != navigation)
            return;
        // Not NavigatingEntity when Lithium's inactive_navigations option is off: nothing to keep in sync then.
        if (mob instanceof NavigatingEntity tracked && tracked.lithium$isRegisteredToWorld()
            && tracked.lithium$getRegisteredNavigation() != navigation)
            tracked.lithium$updateNavigationRegistration();
    }
}
