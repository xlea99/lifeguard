package net.xlea99.lifeguard.mixin.zeta_instance_race;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zeta.util.zetalist.ZetaClientList;

/**
 * The client list's own lookup from a Zeta to its client half, filled the same racy way as the list itself
 * ({@link ZetaListMixin}, which already covers the list this one extends).
 */
@Mixin(ZetaClientList.class)
public abstract class ZetaClientListMixin {

    @Shadow
    private Map<Object, Object> zetasClientMap;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lifeguard$threadSafe(CallbackInfo ci) {
        this.zetasClientMap = new ConcurrentHashMap<>(this.zetasClientMap);
    }
}
