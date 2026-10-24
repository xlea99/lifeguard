package net.xlea99.lifeguard.mixin.supps_captured_mob_jitter;

import net.mehvahdjukaar.supplementaries.api.CapturedMobInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mobs in Supplementaries' jars and cages animate smoothly instead of vibrating. A captured mob only lives through its
 * container's tick. Mobs with one of Supplementaries' capture files get a tick that advances the mob's age; every other
 * mob (the allay, the frog, most modded mobs) gets this plain one, whose tick does nothing. The renderer animates from
 * age plus the partial tick, so with the age stuck at 0 the animation ran 0 to 1 each tick and snapped back: a fast
 * shimmer on anything that idles (wings, antennae). Advancing the age here, as the capture-file tick does, lets it play.
 */
@Mixin(CapturedMobInstance.class)
public abstract class CapturedMobInstanceMixin<T extends Entity> {

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void lifeguard$age(Level level, BlockPos pos, float entityScale, CompoundTag entityData, CallbackInfo ci) {
        // Only the plain instance: subclasses (the capture-file one) age their mob themselves.
        CapturedMobInstance<?> self = (CapturedMobInstance<?>) (Object) this;
        Entity entity = self.getEntityForRenderer();
        if (self.getClass() == CapturedMobInstance.class && entity != null)
            entity.tickCount++;
    }
}
