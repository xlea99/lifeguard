package net.xlea99.lifeguard.mixin.boidsify;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// A mob's goals, for boidsify to swap a fish's own schooling and wandering for its boids goal (and back).
@Mixin(Mob.class)
public interface MobAccessor {
    @Accessor("goalSelector")
    GoalSelector lifeguard$goalSelector();
}
