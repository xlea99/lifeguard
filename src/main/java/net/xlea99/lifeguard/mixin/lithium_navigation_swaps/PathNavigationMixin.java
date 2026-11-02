package net.xlea99.lifeguard.mixin.lithium_navigation_swaps;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.xlea99.lifeguard.entry.lithium_navigation_swaps.LithiumNavigationSwaps;
import net.xlea99.lifeguard.entry.lithium_navigation_swaps.NavigationSwaps;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Every path a navigator follows starts here (the other moveTo overloads build a path and call this one), and Lithium
 * files the navigator as active at this method's return: syncing its record at the head means it files the live one.
 */
@Mixin(PathNavigation.class)
public abstract class PathNavigationMixin {
    @Shadow
    @Final
    protected Mob mob;

    @Inject(method = "moveTo(Lnet/minecraft/world/level/pathfinder/Path;D)Z", at = @At("HEAD"))
    private void lifeguard$syncLithiumNavigationRecord(Path path, double speed, CallbackInfoReturnable<Boolean> cir) {
        if (LithiumNavigationSwaps.ENTRY.active())
            NavigationSwaps.beforePath(this.mob, (PathNavigation) (Object) this);
    }
}
