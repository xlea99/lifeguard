package net.xlea99.lifeguard.mixin.warden_roar_loop;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.monster.warden.AngerManagement;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A Warden stays suspicious of what it can't fight, instead of roaring at it forever.
 *
 * <p>Who a Warden gets angry at is its own check ({@code canTargetEntity}, which rules out armor stands by their exact
 * type only), and who it may keep attacking is the general one ({@code canBeSeenAsEnemy}). Vanilla's mobs pass or fail
 * both alike, but a mod's entity can opt out of being anyone's enemy and still pass the Warden's (Species' Deflector
 * Dummy, an armor stand of its own type). Angry enough, the Warden roared at it and made it its target, the fight threw
 * the target out at once, its anger stayed (only a failed {@code canTargetEntity} clears it), and it roared again, over
 * and over. Its anger at something that can't be an enemy now stops just short of angry: it still sniffs it, grows
 * agitated and hisses at it, but never roars. A player it notices takes its attention as usual.
 */
@Mixin(Warden.class)
public abstract class WardenMixin {

    @WrapOperation(method = "increaseAngerAt(Lnet/minecraft/world/entity/Entity;IZ)V", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/monster/warden/AngerManagement;increaseAnger(Lnet/minecraft/world/entity/Entity;I)I"))
    private int lifeguard$neverAngryAtNonEnemies(AngerManagement anger, Entity entity, int offset, Operation<Integer> original) {
        if (!(entity instanceof LivingEntity living) || living.canBeSeenAsEnemy())
            return original.call(anger, entity, offset);
        int most = AngerLevel.ANGRY.getMinimumAnger() - 1;
        int now = anger.getActiveAnger(entity);
        return now >= most ? now : original.call(anger, entity, Math.min(offset, most - now));
    }
}
