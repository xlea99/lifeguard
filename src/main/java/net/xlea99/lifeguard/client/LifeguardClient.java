package net.xlea99.lifeguard.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.boot.Setups;
import net.xlea99.lifeguard.client.menu.LifeguardScreen;

/**
 * Lifeguard's client-only entry point: the menu, opened from the Config button in NeoForge's Mods list, the client
 * machinery, and every entry's client setup ({@link Setups}).
 */
@Mod(value = Lifeguard.MODID, dist = Dist.CLIENT)
public final class LifeguardClient {
    public LifeguardClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new LifeguardScreen(parent));
        EntryPacks.register(modEventBus, container);
        ClientChecks.register(modEventBus);
        RecolorPack.register(modEventBus);
        Setups.runClient(modEventBus);
    }
}
