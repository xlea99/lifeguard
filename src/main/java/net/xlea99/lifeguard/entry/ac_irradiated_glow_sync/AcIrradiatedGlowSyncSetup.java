package net.xlea99.lifeguard.entry.ac_irradiated_glow_sync;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/**
 * Setup for {@link AcIrradiatedGlowSync}.
 *
 * <p>Alex's Caves draws the radiation glow from the client's copy of a mob's effects, but vanilla only sends a mob's effects to
 * whoever rides it, and Alex's Caves' own message only covers its raygun and radioactive items. So anything else (a nuke, unrefined
 * waste, a gas cloud, a command) irradiates a mob that never glows, and a mob loses its glow once it unloads. This sends Irradiated
 * to the players tracking a mob when it's added, strengthened, removed or expired, and to a player who starts tracking the mob.
 * Clients never expire an effect by themselves, so removal has to be sent too. Removal listens last: a cancelled removal never
 * reaches it.
 */
public final class AcIrradiatedGlowSyncSetup implements EntrySetup {
    private static final ResourceLocation IRRADIATED = ResourceLocation.fromNamespaceAndPath("alexscaves", "irradiated");

    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(AcIrradiatedGlowSync.ENTRY, () -> {
            NeoForge.EVENT_BUS.addListener(AcIrradiatedGlowSyncSetup::onAdded);
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, AcIrradiatedGlowSyncSetup::onRemoved);
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, AcIrradiatedGlowSyncSetup::onExpired);
            NeoForge.EVENT_BUS.addListener(AcIrradiatedGlowSyncSetup::onStartTracking);
        });
    }

    private static void onAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance added = event.getEffectInstance();
        MobEffectInstance old = event.getOldEffectInstance();
        if (added.getEffect().is(IRRADIATED) && (old == null || added.getAmplifier() > old.getAmplifier())
            && entity.level() instanceof ServerLevel level)
            level.getChunkSource().broadcast(entity, new ClientboundUpdateMobEffectPacket(entity.getId(), added, false));
    }

    private static void onRemoved(MobEffectEvent.Remove event) {
        broadcastRemoval(event.getEntity(), event.getEffect());
    }

    private static void onExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null)
            broadcastRemoval(event.getEntity(), event.getEffectInstance().getEffect());
    }

    private static void broadcastRemoval(LivingEntity entity, Holder<MobEffect> effect) {
        if (effect.is(IRRADIATED) && entity.level() instanceof ServerLevel level)
            level.getChunkSource().broadcast(entity, new ClientboundRemoveMobEffectPacket(entity.getId(), effect));
    }

    private static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof LivingEntity target) || !(event.getEntity() instanceof ServerPlayer player))
            return;
        for (MobEffectInstance effect : target.getActiveEffects()) {
            if (effect.getEffect().is(IRRADIATED))
                player.connection.send(new ClientboundUpdateMobEffectPacket(target.getId(), effect, false));
        }
    }
}
