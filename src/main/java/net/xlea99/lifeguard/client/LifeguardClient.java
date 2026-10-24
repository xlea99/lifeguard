package net.xlea99.lifeguard.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.client.menu.LifeguardScreen;

/** Lifeguard's client-only entry point: the menu, opened from the Config button in NeoForge's Mods list. */
@Mod(value = Lifeguard.MODID, dist = Dist.CLIENT)
public final class LifeguardClient {
    public LifeguardClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new LifeguardScreen(parent));
    }
}
