package net.xlea99.lifeguard.entry.cc_bone_flute_checks;

import com.teamabnormals.caverns_and_chasms.core.registry.CCItems;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.player.Player;

/**
 * The server took a Caverns & Chasms Bone Flute command (attack, move, recall, sit, sent by the client as a packet)
 * from anyone who sent one, holding a flute or not and as often as they liked (#45): a modified client could command its
 * rats without a flute and spam the flute's sound every tick. Now a command counts only from a player holding a Bone
 * Flute, at most about once per flute use ({@code mixin.cc_bone_flute_checks.BoneFluteCommandPayloadMixin}).
 */
public final class BoneFluteCommands {
    /**
     * Least time between two flute commands from one player. The flute's own cooldown is 20 ticks, kept by the client;
     * a little slack so a legit command never bounces off network jitter.
     */
    private static final long COMMAND_GAP = 15;
    /** Only touched on the server thread (the command's queued work). */
    private static final Map<Player, Long> LAST_COMMAND = new WeakHashMap<>();

    private BoneFluteCommands() {
    }

    /** Whether a Bone Flute command from this player counts: they hold a flute and haven't just commanded. */
    public static boolean mayCommand(Player player) {
        if (player == null || !player.getMainHandItem().is(CCItems.BONE_FLUTE.get()) && !player.getOffhandItem().is(CCItems.BONE_FLUTE.get()))
            return false;
        long now = player.level().getGameTime();
        Long last = LAST_COMMAND.get(player);
        if (last != null && now - last < COMMAND_GAP && now >= last)
            return false;
        LAST_COMMAND.put(player, now);
        return true;
    }
}
