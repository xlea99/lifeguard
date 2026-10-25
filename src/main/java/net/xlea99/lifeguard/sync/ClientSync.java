package net.xlea99.lifeguard.sync;

import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.boot.Boot;

/** Leaving a server puts the client back on its own settings, so the menu at the title screen shows the local file. */
@EventBusSubscriber(modid = Lifeguard.MODID, value = Dist.CLIENT)
public final class ClientSync {
    private ClientSync() {
    }

    /**
     * A game that loaded a registry-shaping entry can't play on a server that never said it loaded the same: that
     * server has no Lifeguard (or one from before the check), so its blocks don't match ours.
     */
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        boolean answered = RegistryShape.takeReceived();
        if (answered || event.getConnection().isMemoryConnection() || Boot.loadedRegistryShapers().isEmpty())
            return;
        event.getConnection().disconnect(Component.translatable("lifeguard.disconnect.registry_shape.no_lifeguard",
            String.join(", ", Boot.loadedRegistryShapers())));
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        RemoteServer.stopFollowing();
    }
}
