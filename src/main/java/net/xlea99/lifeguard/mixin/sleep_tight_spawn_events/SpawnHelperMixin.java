package net.xlea99.lifeguard.mixin.sleep_tight_spawn_events;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.sleep_tight.core.SpawnHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Sleep Tight's wake-up monsters and bedbugs fire NeoForge's spawn event, like every other spawn.
 *
 * <p>They're spawned through {@code SpawnHelper.doSpawnMob}, which calls {@code Mob.finalizeSpawn} directly, so
 * {@code FinalizeSpawnEvent} never fired for them: spawn rules (In Control), mob replacements (Quark's Forgotten) and
 * per-spawn setup (Alex's Caves' creepers) all missed them. NeoForge's own spawners go through
 * {@code EventHooks.finalizeMobSpawn} (the event, then {@code finalizeSpawn} unless it's cancelled; a spawn the event
 * blocks is then stopped by NeoForge itself), and so do these now.
 */
@Mixin(SpawnHelper.class)
public abstract class SpawnHelperMixin {

    @WrapOperation(method = "doSpawnMob", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/Mob;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;"))
    private static SpawnGroupData lifeguard$fireSpawnEvent(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty,
                                                           MobSpawnType type, @Nullable SpawnGroupData data,
                                                           Operation<SpawnGroupData> original) {
        return EventHooks.finalizeMobSpawn(mob, level, difficulty, type, data);
    }
}
