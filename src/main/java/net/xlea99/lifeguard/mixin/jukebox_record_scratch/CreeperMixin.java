package net.xlea99.lifeguard.mixin.jukebox_record_scratch;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Creeper;
import net.xlea99.lifeguard.entry.jukebox_record_scratch.RecordScratch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The moment a creeper's fuse catches (its one hiss in tick, flint and steel included), nearby jukeboxes scratch. */
@Mixin(Creeper.class)
public abstract class CreeperMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/monster/Creeper;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void lifeguard$scratchNearbyRecords(CallbackInfo ci) {
        Creeper creeper = (Creeper) (Object) this;
        if (creeper.level() instanceof ServerLevel level)
            RecordScratch.hiss(level, creeper);
    }
}
