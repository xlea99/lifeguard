package net.xlea99.lifeguard.mixin.zeta_instance_race;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zeta.util.zetalist.ZetaList;

/**
 * Makes Zeta's list of loaded Zeta mods thread-safe (also the client list, which is built on this one).
 *
 * <p>Every Zeta mod (Quark, and Zeta itself) adds itself to this list when its {@code Zeta} is created. It was a plain
 * {@code HashSet} made for 2. Zeta's own instance is created in its mod constructor, Quark's in the {@code Quark}
 * class's static initializer, and NeoForge constructs mods in parallel; on the rare launch where the two land at once,
 * the set loses one of them. Quark then wasn't in the list for that whole launch: Zeta's biome modifier never added its
 * mob spawns, it missed load events sent through the list, and mods looking it up there (Dye the World) crashed. A
 * concurrent set can't lose one.
 */
@Mixin(ZetaList.class)
public abstract class ZetaListMixin {

    @Shadow
    @Final
    @Mutable
    private Set<Object> knownZetas;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lifeguard$threadSafe(CallbackInfo ci) {
        Set<Object> zetas = ConcurrentHashMap.newKeySet();
        zetas.addAll(this.knownZetas);
        this.knownZetas = zetas;
    }
}
