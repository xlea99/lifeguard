package net.xlea99.lifeguard.entry.supps_respects_mob_griefing;

import net.mehvahdjukaar.supplementaries.common.misc.explosion.CannonBallExplosion;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.ExplosionEvent;

/**
 * A cannonball fired by a mob (a Plunderer turning a cannon on you) breaks no blocks while mob griefing is off, like
 * every other mob explosion. Supplementaries' bombs already ask; its cannonballs never did. The ball still bursts and
 * still hurts what it hits. Cannons fired by players or by redstone (no shooter) are untouched. Only loaded when the
 * entry is active, so only with Supplementaries installed.
 */
public final class MobFiredCannonballs {
    private MobFiredCannonballs() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(MobFiredCannonballs::onDetonate);
    }

    private static void onDetonate(ExplosionEvent.Detonate event) {
        if (!(event.getExplosion() instanceof CannonBallExplosion explosion))
            return;
        LivingEntity shooter = explosion.getIndirectSourceEntity();
        if (shooter == null || shooter instanceof Player)
            return;
        if (!EventHooks.canEntityGrief(event.getLevel(), shooter))
            event.getAffectedBlocks().clear();
    }
}
