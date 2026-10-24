package net.xlea99.lifeguard.entry.faucet_pouring_recipes;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.xlea99.lifeguard.Lifeguard;

/**
 * The server's faucet pouring recipes, as their JSON files, for the client's recipe viewer. Registered whenever
 * Supplementaries is installed (on both sides alike); the server only sends it with the entry on. Optional: a client
 * without Lifeguard can still join.
 */
public record PouringRecipesPayload(List<String> json) implements CustomPacketPayload {
    public static final Type<PouringRecipesPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "faucet_pouring_recipes"));
    public static final StreamCodec<FriendlyByteBuf, PouringRecipesPayload> STREAM_CODEC =
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).map(PouringRecipesPayload::new, PouringRecipesPayload::json).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToClient(TYPE, STREAM_CODEC, PouringRecipesPayload::receive);
    }

    private static void receive(PouringRecipesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientPouringRecipes.receive(payload.json()));
    }
}
