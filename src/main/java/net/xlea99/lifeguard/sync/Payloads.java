package net.xlea99.lifeguard.sync;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Where every play payload of ours is sent from. They're all optional, so a client without Lifeguard can join, and
 * NeoForge throws on sending one to a client that never registered it: the send is skipped for those. A workspace test
 * keeps every other class off {@link PacketDistributor}.
 */
public final class Payloads {
    private Payloads() {
    }

    /** Sends {@code payload} to {@code player}, unless their client can't take it (no Lifeguard, or an older one). */
    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        if (player.connection.hasChannel(payload))
            PacketDistributor.sendToPlayer(player, payload);
    }
}
