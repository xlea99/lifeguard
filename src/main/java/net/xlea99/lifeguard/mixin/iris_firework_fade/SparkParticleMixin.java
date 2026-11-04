package net.xlea99.lifeguard.mixin.iris_firework_fade;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.xlea99.lifeguard.entry.sexy_runic_glints.Shaderpacks;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Lets firework sparks fade out again when no shaderpack is on.
 *
 * <p>Iris adds a {@code getRenderType} to the spark that always answers opaque, even with no shaderpack, so sparks stay solid and
 * then pop out instead of melting away. A plain method from a higher priority mixin replaces it. With a shaderpack on, this keeps
 * Iris's answer.
 */
@Mixin(targets = "net.minecraft.client.particle.FireworkParticles$SparkParticle", priority = 1500)
public abstract class SparkParticleMixin extends SimpleAnimatedParticle {
    private SparkParticleMixin(ClientLevel level, double x, double y, double z, SpriteSet sprites, float gravity) {
        super(level, x, y, z, sprites, gravity);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return Shaderpacks.active() ? ParticleRenderType.PARTICLE_SHEET_OPAQUE : super.getRenderType();
    }
}
