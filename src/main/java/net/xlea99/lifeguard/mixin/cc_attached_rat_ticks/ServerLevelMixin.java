package net.xlea99.lifeguard.mixin.cc_attached_rat_ticks;

import com.teamabnormals.caverns_and_chasms.common.entity.animal.rat.Rat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A Caverns & Chasms rat clinging to a mob or player is ticked by its host, right after the host's own tick (C&C's
 * ServerLevelMixin.updateRats -> tickRats -> Rat.tickAttached). On the client C&C also skips the rat's own tick; on the
 * server it didn't, so the rat ran twice a tick (#43): it bit twice as often, mobs shook it off twice as fast, and a
 * player's grip check (which compares the camera with the last tick) misread the second run. Here the server skips the
 * rat's own tick too, while it clings to a live host in its own level, as the client does.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    private void lifeguard$leaveToHost(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof Rat rat) || !rat.isAttachedToEntity())
            return;
        LivingEntity host = rat.getAttachedEntity();
        if (host != null && !host.isRemoved() && host.level() == rat.level())
            ci.cancel();
    }
}
