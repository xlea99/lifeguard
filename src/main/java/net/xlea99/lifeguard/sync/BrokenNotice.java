package net.xlea99.lifeguard.sync;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.xlea99.lifeguard.boot.BrokenEntries;
import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;

/**
 * Operators (and a singleplayer host) hear about {@link BrokenEntries broken entries} as they join: a log line is easy
 * to miss. Outside boot on purpose: boot runs in the mixin plugin, before Minecraft's classes may load.
 */
public final class BrokenNotice {
    private BrokenNotice() {
    }

    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        List<Entry> broken = Entries.REGISTRY.broken();
        if (broken.isEmpty() || !(event.getEntity() instanceof ServerPlayer player))
            return;
        MinecraftServer server = player.getServer();
        if (server == null || !player.hasPermissions(2) && !server.isSingleplayerOwner(player.getGameProfile()))
            return;
        MutableComponent names = Component.empty();
        for (int i = 0; i < broken.size(); i++) {
            if (i > 0)
                names.append(", ");
            names.append(Component.translatableWithFallback(broken.get(i).nameKey(), broken.get(i).id()));
        }
        // With a fallback: the player may not have Lifeguard's lang file.
        player.sendSystemMessage(Component.translatableWithFallback("lifeguard.chat.broken",
            "Lifeguard: broken this session, so off: %s. A mod they hook has changed; the log says what didn't fit.",
            names).withStyle(ChatFormatting.RED));
    }
}
