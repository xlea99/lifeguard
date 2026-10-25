package net.xlea99.lifeguard.sync;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.Lifeguard;

/**
 * The registry-shaping entries the server loaded, by id, sent to a joining client so it can check it loaded the same
 * ones. Its own payload, apart from the settings, so clients from before it existed still join servers without any.
 */
public record RegistryShapePayload(List<String> ids) implements CustomPacketPayload {
    public static final Type<RegistryShapePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "registry_shape"));
    public static final StreamCodec<FriendlyByteBuf, RegistryShapePayload> STREAM_CODEC =
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).map(RegistryShapePayload::new, RegistryShapePayload::ids)
            .cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
