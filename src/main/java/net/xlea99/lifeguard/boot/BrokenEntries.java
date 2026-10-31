package net.xlea99.lifeguard.boot;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Switches an entry off for the session because its code no longer fits what it hooks, and says so loudly: one ERROR
 * per entry naming what didn't fit, the mods involved with their versions, and everything that goes dark with it. The
 * config is never touched, so a version that fits brings the entry back as the player left it. Operators also hear on
 * joining ({@link net.xlea99.lifeguard.sync.BrokenNotice}).
 *
 * <p>Runs in the mixin plugin, before Minecraft's classes may load: keep Minecraft out of this class.
 */
public final class BrokenEntries {
    private static final Logger LOGGER = LoggerFactory.getLogger("Lifeguard");

    private BrokenEntries() {
    }

    /** {@code what}: what didn't match, e.g. "SpeakerBlockMixin: SpeakerBlock.use no longer calls playSound". */
    public static void mark(Entry entry, String what) {
        if (entry.broken()) {
            // Runtime code can find the same thing on every call: say it once.
            if (!what.equals(entry.brokenReason()))
                LOGGER.error("Lifeguard: {} is also broken by: {}", entry.id(), what);
            return;
        }
        entry.markBroken(what);
        List<String> dark = new ArrayList<>();
        for (Entry dependent : Entries.REGISTRY.dependents(entry))
            dark.add(dependent.id());
        LOGGER.error("Lifeguard: {} is BROKEN, so it's off this session: {} ({}).{} Its setting is kept: it comes back"
                + " once a version that fits is installed.", entry.id(), what, versions(entry),
            dark.isEmpty() ? "" : " Off with it, since they need it: " + String.join(", ", dark) + ".");
    }

    /** The entry's required mods as "supplementaries 3.1.2", for the report: what changed is usually one of these. */
    private static String versions(Entry entry) {
        if (entry.requiredMods().isEmpty())
            return "Minecraft " + version("minecraft") + ", NeoForge " + version("neoforge");
        List<String> out = new ArrayList<>();
        for (String modId : entry.requiredMods())
            out.add(modId + " " + version(modId));
        return String.join(", ", out);
    }

    private static String version(String modId) {
        for (ModInfo mod : LoadingModList.get().getMods())
            if (mod.getModId().equals(modId))
                return mod.getVersion().toString();
        return "(not installed)";
    }
}
