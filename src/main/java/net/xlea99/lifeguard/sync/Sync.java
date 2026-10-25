package net.xlea99.lifeguard.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.xlea99.lifeguard.boot.Boot;
import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.ServerSnapshot;

/**
 * Sends the server's entry settings to each joining client, and checks the two agree on registry-shaping entries.
 *
 * <p>It happens during the configuration phase, before the client enters the world, so nothing of ours ever runs on a
 * remote server's world with the player's local settings. The payloads are optional: a client without Lifeguard can
 * still join a server that has it, as far as this is concerned, unless the server loaded a registry-shaping entry: then
 * its registries differ from any client's that didn't, and that client is turned away with a message instead.
 */
public final class Sync {
    private static final ConfigurationTask.Type TASK = new ConfigurationTask.Type(
        ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "send_server_entries"));
    private static final ConfigurationTask.Type SHAPE_TASK = new ConfigurationTask.Type(
        ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "check_registry_shape"));

    private Sync() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional()
            .configurationToClient(SyncPayload.TYPE, SyncPayload.STREAM_CODEC, Sync::receive)
            .configurationToClient(RegistryShapePayload.TYPE, RegistryShapePayload.STREAM_CODEC, Sync::receiveShape);
    }

    public static void registerTasks(RegisterConfigurationTasksEvent event) {
        ServerConfigurationPacketListener listener = event.getListener();
        if (listener.hasChannel(SyncPayload.TYPE))
            event.register(new SendTask(listener));
        // Always sent when the client can take it, even empty: a client that loaded one the server didn't is refused too.
        if (listener.hasChannel(RegistryShapePayload.TYPE))
            event.register(new ShapeTask(listener));
        else if (!Boot.loadedRegistryShapers().isEmpty())
            event.register(new RefuseTask(listener));
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

    private static void receiveShape(RegistryShapePayload payload, IPayloadContext context) {
        RegistryShape.received();
        // Our own integrated server loaded exactly what this game did.
        if (context.connection().isMemoryConnection())
            return;
        Component mismatch = mismatch(Set.copyOf(payload.ids()), Boot.loadedRegistryShapers());
        if (mismatch != null)
            context.disconnect(mismatch);
    }

    /** Why this client can't join, or null when it loaded exactly the registry-shaping entries the server did. */
    static Component mismatch(Set<String> server, Set<String> local) {
        if (server.equals(local))
            return null;
        List<Component> serverOnly = new ArrayList<>();
        List<Component> localOnly = new ArrayList<>();
        for (String id : server)
            if (!local.contains(id))
                serverOnly.add(entryName(id));
        for (String id : local)
            if (!server.contains(id))
                localOnly.add(entryName(id));
        MutableComponent message = Component.translatable("lifeguard.disconnect.registry_shape");
        if (!serverOnly.isEmpty())
            message.append("\n\n").append(Component.translatable("lifeguard.disconnect.registry_shape.server_only",
                joined(serverOnly)));
        if (!localOnly.isEmpty())
            message.append("\n\n").append(Component.translatable("lifeguard.disconnect.registry_shape.local_only",
                joined(localOnly)));
        return message.append("\n\n").append(Component.translatable("lifeguard.disconnect.registry_shape.fix"));
    }

    /** The entry's display name, or its raw id when this version doesn't have it (a newer server). */
    private static Component entryName(String id) {
        return Entries.REGISTRY.get(id).<Component>map(entry -> Component.translatable(entry.nameKey()))
            .orElse(Component.literal(id));
    }

    private static Component joined(List<Component> names) {
        MutableComponent out = Component.empty();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0)
                out.append(", ");
            out.append(names.get(i));
        }
        return out;
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

    private record ShapeTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
        @Override
        public void run(Consumer<CustomPacketPayload> sender) {
            sender.accept(new RegistryShapePayload(List.copyOf(Boot.loadedRegistryShapers())));
            // A client that disagrees disconnects itself while handling it, before the play phase.
            listener.finishCurrentTask(SHAPE_TASK);
        }

        @Override
        public Type type() {
            return SHAPE_TASK;
        }
    }

    /** A client that can't hear about registry shapes (no Lifeguard, or one older than the check) can't match them. */
    private record RefuseTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
        @Override
        public void run(Consumer<CustomPacketPayload> sender) {
            // Plain English: this client may not have Lifeguard's lang file.
            listener.disconnect(Component.literal("This server runs Lifeguard entries that change blocks ("
                + String.join(", ", Boot.loadedRegistryShapers())
                + "), so it needs a client with an up-to-date Lifeguard and the same entries on."));
        }

        @Override
        public Type type() {
            return SHAPE_TASK;
        }
    }
}
