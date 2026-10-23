package net.xlea99.lifeguard.sync;

import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.ServerSnapshot;

/**
 * Sends the server's entry settings to each joining client.
 *
 * <p>It happens during the configuration phase, before the client enters the world, so nothing of ours ever runs on a
 * remote server's world with the player's local settings. The payload is optional: a client without Lifeguard can
 * still join a server that has it, as far as this is concerned.
 */
public final class Sync {
    private static final ConfigurationTask.Type TASK = new ConfigurationTask.Type(
        ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "send_server_entries"));

    private Sync() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional()
            .configurationToClient(SyncPayload.TYPE, SyncPayload.STREAM_CODEC, Sync::receive);
    }

    public static void registerTasks(RegisterConfigurationTasksEvent event) {
        ServerConfigurationPacketListener listener = event.getListener();
        if (listener.hasChannel(SyncPayload.TYPE))
            event.register(new SendTask(listener));
    }

    /** Any server starting in this game (singleplayer, LAN) runs on the local file, whatever the last join left. */
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        RemoteServer.stopFollowing();
    }

    private static void receive(SyncPayload payload, IPayloadContext context) {
        // Our own integrated server is the local state already; there's nothing to follow.
        if (context.connection().isMemoryConnection())
            return;
        context.enqueueWork(() -> RemoteServer.follow(payload.snapshot()));
    }

    private record SendTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
        @Override
        public void run(Consumer<CustomPacketPayload> sender) {
            sender.accept(new SyncPayload(ServerSnapshot.capture(Entries.REGISTRY)));
            // Nothing to wait for: the client applies it before the play phase starts, since packets arrive in order.
            listener.finishCurrentTask(TASK);
        }

        @Override
        public Type type() {
            return TASK;
        }
    }
}
