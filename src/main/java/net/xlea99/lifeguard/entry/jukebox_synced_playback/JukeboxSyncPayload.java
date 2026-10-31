package net.xlea99.lifeguard.entry.jukebox_synced_playback;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.xlea99.lifeguard.Lifeguard;

/**
 * A jukebox playing {@code song} (its id in the jukebox song registry, as vanilla's play event), {@code elapsed} ticks
 * into it, heard {@code range} blocks away ({@link JukeboxSync}). Optional: a client without Lifeguard can still join.
 */
public record JukeboxSyncPayload(BlockPos pos, int song, long elapsed, int range) implements CustomPacketPayload {
    public static final Type<JukeboxSyncPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "jukebox_sync"));
    public static final StreamCodec<FriendlyByteBuf, JukeboxSyncPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, JukeboxSyncPayload::pos,
        ByteBufCodecs.VAR_INT, JukeboxSyncPayload::song,
        ByteBufCodecs.VAR_LONG, JukeboxSyncPayload::elapsed,
        ByteBufCodecs.VAR_INT, JukeboxSyncPayload::range,
        JukeboxSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Both of synced playback's payloads. */
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional()
            .playToClient(TYPE, STREAM_CODEC, JukeboxSyncPayload::receive)
            .playToClient(JukeboxStopPayload.TYPE, JukeboxStopPayload.STREAM_CODEC, JukeboxStopPayload::receive);
    }

    private static void receive(JukeboxSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientJukeboxSync.onSync(payload.pos(), payload.song(), payload.elapsed(), payload.range()));
    }
}
