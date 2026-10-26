package net.xlea99.lifeguard.mixin.sleep_tight_kill_stats;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.sleep_tight.core.ModEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Every kill a player makes counted once in Statistics > Mobs, not twice ("You have killed 2 Seal").
 *
 * <p>On every death Sleep Tight asks {@code killer.killedEntity(level, killed)} whether to apply its Invigorated
 * effect. But for a player that isn't a question: vanilla's {@code Player.killedEntity} awards the per-mob "killed"
 * stat (and always answers true), and vanilla calls it itself a moment later in {@code LivingEntity.die}. So a player
 * killer gets Sleep Tight's answer (true) without the call; any other killer keeps Sleep Tight's behavior.
 */
@Mixin(ModEvents.class)
public abstract class ModEventsMixin {

    @WrapOperation(method = "onEntityKilled", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/Entity;killedEntity(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private static boolean lifeguard$askWithoutAwardingTheStat(Entity killer, ServerLevel level, LivingEntity killed,
                                                               Operation<Boolean> original) {
        return killer instanceof Player || original.call(killer, level, killed);
    }
}
