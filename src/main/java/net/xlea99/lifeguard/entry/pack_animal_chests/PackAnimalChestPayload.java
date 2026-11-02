package net.xlea99.lifeguard.entry.pack_animal_chests;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.xlea99.lifeguard.Lifeguard;

/** The pack animal {@code entity} wears Quark's {@code chest} (an item id, "" for none or a plain one): {@link PackAnimalChest}. */
public record PackAnimalChestPayload(int entity, String chest) implements CustomPacketPayload {
    public static final Type<PackAnimalChestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "pack_animal_chest"));
    public static final StreamCodec<ByteBuf, PackAnimalChestPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, PackAnimalChestPayload::entity, ByteBufCodecs.STRING_UTF8, PackAnimalChestPayload::chest,
        PackAnimalChestPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToClient(TYPE, STREAM_CODEC, PackAnimalChestPayload::receive);
    }

    private static void receive(PackAnimalChestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientPackAnimalChests.received(payload.entity(), payload.chest()));
    }
}
