package net.xlea99.lifeguard.sync;

import io.netty.handler.codec.DecoderException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.core.ServerSnapshot;
import net.xlea99.lifeguard.core.Switch;

/** The server's entry settings, sent to a joining client during configuration. */
public record SyncPayload(ServerSnapshot snapshot) implements CustomPacketPayload {
    public static final Type<SyncPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "server_entries"));
    public static final StreamCodec<FriendlyByteBuf, SyncPayload> STREAM_CODEC =
        StreamCodec.ofMember(SyncPayload::write, SyncPayload::read);

    private static final byte INT = 0;
    private static final byte DECIMAL = 1;
    private static final byte BOOL = 2;

    private void write(FriendlyByteBuf buf) {
        buf.writeUtf(snapshot.profile());
        buf.writeVarInt(snapshot.entries().size());
        for (Map.Entry<String, ServerSnapshot.EntryState> entry : snapshot.entries().entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeUtf(entry.getValue().setting().id());
            Map<String, Object> values = entry.getValue().values();
            buf.writeVarInt(values.size());
            for (Map.Entry<String, Object> value : values.entrySet()) {
                buf.writeUtf(value.getKey());
                switch (value.getValue()) {
                    case Integer number -> {
                        buf.writeByte(INT);
                        buf.writeInt(number);
                    }
                    case Double number -> {
                        buf.writeByte(DECIMAL);
                        buf.writeDouble(number);
                    }
                    case Boolean flag -> {
                        buf.writeByte(BOOL);
                        buf.writeBoolean(flag);
                    }
                    default -> throw new IllegalStateException("sub-value " + entry.getKey() + "." + value.getKey()
                        + " holds a " + value.getValue().getClass().getSimpleName() + ", which can't be synced");
                }
            }
        }
    }

    private static SyncPayload read(FriendlyByteBuf buf) {
        String profile = buf.readUtf();
        int entryCount = buf.readVarInt();
        Map<String, ServerSnapshot.EntryState> entries = new LinkedHashMap<>();
        for (int i = 0; i < entryCount; i++) {
            String id = buf.readUtf();
            Switch setting = Switch.byId(buf.readUtf()).orElse(Switch.AUTO);
            int valueCount = buf.readVarInt();
            Map<String, Object> values = new LinkedHashMap<>();
            for (int j = 0; j < valueCount; j++) {
                String name = buf.readUtf();
                byte kind = buf.readByte();
                values.put(name, switch (kind) {
                    case INT -> buf.readInt();
                    case DECIMAL -> buf.readDouble();
                    case BOOL -> buf.readBoolean();
                    default -> throw new DecoderException("Lifeguard: unknown sub-value kind " + kind + " for " + id + "." + name);
                });
            }
            entries.put(id, new ServerSnapshot.EntryState(setting, values));
        }
        return new SyncPayload(new ServerSnapshot(profile, entries));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
