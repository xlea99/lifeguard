package net.xlea99.lifeguard.mixin.boidsify;

import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// How fast a fish's own wandering swims it, for boidsify's speeds (multiples of the fish's own swimming speed).
@Mixin(RandomStrollGoal.class)
public interface RandomStrollGoalAccessor {
    @Accessor("speedModifier")
    double lifeguard$speedModifier();
}
