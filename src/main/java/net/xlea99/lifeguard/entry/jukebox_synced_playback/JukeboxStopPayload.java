package net.xlea99.lifeguard.entry.jukebox_synced_playback;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.xlea99.lifeguard.Lifeguard;

/** The jukebox at {@code pos} stopped, for listeners it was carried to past vanilla's reach ({@link JukeboxSync}). */
public record JukeboxStopPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<JukeboxStopPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "jukebox_stop"));
    public static final StreamCodec<FriendlyByteBuf, JukeboxStopPayload> STREAM_CODEC =
        BlockPos.STREAM_CODEC.map(JukeboxStopPayload::new, JukeboxStopPayload::pos).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    static void receive(JukeboxStopPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientJukeboxSync.onStop(payload.pos()));
    }
}
