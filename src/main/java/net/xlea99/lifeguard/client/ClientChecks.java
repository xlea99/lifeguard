package net.xlea99.lifeguard.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.core.Entry;

/**
 * Checks an active entry asks for after every resource reload, once models are baked: that its assets actually won (no
 * other pack covered them, nothing failed to load). A failure is loud, never silent: an error in the log naming the
 * entry and what's wrong, and a toast on screen.
 */
public final class ClientChecks {
    private static final List<Check> CHECKS = new ArrayList<>();

    private ClientChecks() {
    }

    /** {@code problem} returns what's wrong, or null when all is well. Register at startup, for an active entry only. */
    public static synchronized void register(Entry entry, Supplier<String> problem) {
        CHECKS.add(new Check(entry, problem));
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener((RegisterClientReloadListenersEvent event) ->
            event.registerReloadListener((ResourceManagerReloadListener) manager ->
                // Queued behind the rest of the reload's work on the main thread, so the models are in place.
                Minecraft.getInstance().execute(ClientChecks::runAll)));
    }

    private static synchronized void runAll() {
        for (Check check : CHECKS) {
            String problem;
            try {
                problem = check.problem().get();
            } catch (RuntimeException e) {
                problem = "the check itself failed: " + e;
            }
            if (problem == null)
                continue;
            Lifeguard.LOGGER.error("Lifeguard: {} isn't working as it should: {}", check.entry().id(), problem);
            SystemToast.addOrUpdate(Minecraft.getInstance().getToasts(), SystemToast.SystemToastId.PACK_LOAD_FAILURE,
                Component.translatable("lifeguard.check.failed", Component.translatable(check.entry().nameKey())),
                Component.literal(problem));
        }
    }

    private record Check(Entry entry, Supplier<String> problem) {
    }
}
