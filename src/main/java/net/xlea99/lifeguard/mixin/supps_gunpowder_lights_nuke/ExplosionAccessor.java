package net.xlea99.lifeguard.mixin.supps_gunpowder_lights_nuke;

import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Reads the level a gunpowder explosion is in (see {@code GunpowderExplosionMixin}). */
@Mixin(Explosion.class)
public interface ExplosionAccessor {
    @Accessor("level")
    Level lifeguard$level();
}
