package net.xlea99.lifeguard.entry.chest_boat_chests;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.xlea99.lifeguard.Lifeguard;

/** The chest boat {@code entity} carries {@code chest} (an item id), sent as a player starts seeing it ({@link BoatChest}). */
public record BoatChestPayload(int entity, String chest) implements CustomPacketPayload {
    public static final Type<BoatChestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "boat_chest"));
    public static final StreamCodec<ByteBuf, BoatChestPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, BoatChestPayload::entity, ByteBufCodecs.STRING_UTF8, BoatChestPayload::chest, BoatChestPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToClient(TYPE, STREAM_CODEC, BoatChestPayload::receive);
    }

    private static void receive(BoatChestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientBoatChests.received(payload.entity(), payload.chest()));
    }
}
