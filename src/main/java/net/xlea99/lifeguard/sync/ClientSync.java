package net.xlea99.lifeguard.sync;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.xlea99.lifeguard.Lifeguard;

/** Leaving a server puts the client back on its own settings, so the menu at the title screen shows the local file. */
@EventBusSubscriber(modid = Lifeguard.MODID, value = Dist.CLIENT)
public final class ClientSync {
    private ClientSync() {
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        RemoteServer.stopFollowing();
    }
}
