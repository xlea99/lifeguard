package net.xlea99.lifeguard.boot;

import com.bawnorton.mixinsquared.canceller.MixinCancellerRegistrar;
import java.util.List;
import java.util.Set;
import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.MixinGate;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decides, entry by entry, which of Lifeguard's mixins exist this session (see {@link MixinGate} for the rule), and
 * which of other mods' mixins entries switch off ({@link LifeguardMixinCanceller}).
 */
public final class LifeguardMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger("Lifeguard");

    @Override
    public void onLoad(String mixinPackage) {
        if (!MixinGate.MIXIN_ROOT.equals(mixinPackage + "."))
            throw new IllegalStateException("lifeguard.mixins.json's package is " + mixinPackage
                + ", but the gate expects " + MixinGate.MIXIN_ROOT);
        Boot.start();
        // Other mods' mixins are applied class by class long after every config's onLoad, so this is in time.
        MixinCancellerRegistrar.register(new LifeguardMixinCanceller());
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean apply = MixinGate.shouldApply(mixinClassName, Entries.REGISTRY, Boot.dedicatedServer());
        LOGGER.debug("Lifeguard mixin {} -> {}: {}", mixinClassName, targetClassName, apply ? "applied" : "skipped");
        return apply;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
