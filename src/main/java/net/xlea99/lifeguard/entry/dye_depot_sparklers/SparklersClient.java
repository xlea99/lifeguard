package net.xlea99.lifeguard.entry.dye_depot_sparklers;

import com.teamabnormals.caverns_and_chasms.client.particle.SparklerParticle;
import com.teamabnormals.caverns_and_chasms.client.particle.SparklerSeedParticle;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * Draws our sparklers' particles with C&C's own renderers, as C&C does for its 16
 * (CCParticleTypes.RegisterParticles.registerSparkler): the spark from its sprites, the emitter as a special particle
 * that spawns sparks. Their textures come from client/RecolorPack.
 */
public final class SparklersClient {
    private SparklersClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(SparklersClient::onRegisterParticles);
    }

    private static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        for (var pair : Sparklers.PARTICLE_PAIRS) {
            var spark = pair.getFirst();
            event.registerSpriteSet(spark.get(), SparklerParticle.Provider::new);
            event.registerSpecial(pair.getSecond().get(), new SparklerSeedParticle.Provider(spark::get));
        }
    }
}
