package net.xlea99.lifeguard.entry.sexy_runic_glints;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.xlea99.lifeguard.Lifeguard;

/** Client setup for the entry: its settings file (reloaded with resources) and its shader. Only when it's active. */
public final class GlintsClient {
    private GlintsClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegisterClientReloadListenersEvent.class,
            event -> event.registerReloadListener((ResourceManagerReloadListener) GlintMaterials::load));
        modEventBus.addListener(RegisterShadersEvent.class, event -> {
            try {
                GlintShader.register(event);
            } catch (Exception e) {
                Lifeguard.LOGGER.error("Lifeguard couldn't load its glint shader; rune glints stay plain", e);
            }
        });
    }
}
