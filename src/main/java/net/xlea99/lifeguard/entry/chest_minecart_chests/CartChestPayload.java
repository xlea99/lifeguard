package net.xlea99.lifeguard.entry.chest_minecart_chests;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.xlea99.lifeguard.Lifeguard;

/** The chest minecart {@code entity} carries {@code chest} (an item id), sent as a player starts seeing it ({@link CartChest}). */
public record CartChestPayload(int entity, String chest) implements CustomPacketPayload {
    public static final Type<CartChestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "minecart_chest"));
    public static final StreamCodec<ByteBuf, CartChestPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, CartChestPayload::entity, ByteBufCodecs.STRING_UTF8, CartChestPayload::chest, CartChestPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToClient(TYPE, STREAM_CODEC, CartChestPayload::receive);
    }

    private static void receive(CartChestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientCartChests.received(payload.entity(), payload.chest()));
    }
}
