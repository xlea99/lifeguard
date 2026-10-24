package net.xlea99.lifeguard.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.client.menu.LifeguardScreen;
import net.xlea99.lifeguard.entry.sexy_runic_glints.GlintsClient;
import net.xlea99.lifeguard.entry.sexy_runic_glints.SexyRunicGlints;

/**
 * Lifeguard's client-only entry point: the menu, opened from the Config button in NeoForge's Mods list, and client setup
 * for the active entries that need it.
 */
@Mod(value = Lifeguard.MODID, dist = Dist.CLIENT)
public final class LifeguardClient {
    public LifeguardClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new LifeguardScreen(parent));
        if (SexyRunicGlints.ENTRY.active())
            GlintsClient.register(modEventBus);
    }
}
