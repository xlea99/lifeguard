package net.xlea99.lifeguard.mixin.cc_deeper_hats;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.teamabnormals.caverns_and_chasms.core.other.CCEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A creeper spawning in a cave is swapped for a Deeper or Evendeeper (CCEvents.replaceCreeperSpawn), which gets its
 * spawn setup, where it picks its hat, before it's moved to the creeper's spot: so the hats that depend on where it
 * stands (the biome's cave growths, a structure) were judged at the world's origin (#41). The replacement now stands
 * on the creeper's spot first; Caverns & Chasms moves it there again afterwards, to the same place.
 */
@Mixin(CCEvents.class)
public abstract class CCEventsMixin {
    @WrapOperation(method = "replaceCreeperSpawn", at = @At(value = "INVOKE",
        target = "Lnet/neoforged/neoforge/event/EventHooks;finalizeMobSpawn(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;"))
    private static SpawnGroupData lifeguard$setUpWhereItSpawns(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty,
                                                              MobSpawnType spawnType, SpawnGroupData data, Operation<SpawnGroupData> original,
                                                              @Local(argsOnly = true) Creeper creeper) {
        if (mob != null)
            mob.copyPosition(creeper);
        return original.call(mob, level, difficulty, spawnType, data);
    }
}
