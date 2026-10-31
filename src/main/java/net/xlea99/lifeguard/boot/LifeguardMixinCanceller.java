package net.xlea99.lifeguard.boot;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import java.util.List;
import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Switches off the other mods' mixins an active entry cancels ({@link Entry#cancelsMixins}), as each is applied. */
public final class LifeguardMixinCanceller implements MixinCanceller {
    private static final Logger LOGGER = LoggerFactory.getLogger("Lifeguard");

    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        for (Entry entry : Entries.REGISTRY.entries()) {
            if (entry.cancels(mixinClassName)) {
                LOGGER.info("Lifeguard {}: switched off {}", entry.id(), mixinClassName);
                return true;
            }
        }
        return false;
    }
}
