package net.xlea99.lifeguard.entry.jukebox_synced_playback;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.JukeboxSong;
import net.xlea99.lifeguard.sync.Payloads;

/**
 * Jukebox music follows the server. The jukebox keeps its song's time (vanilla counts it, to know when it's over), and
 * while it plays it tells everyone in earshot, once a second, which song and how far in. A client that isn't playing it
 * (just arrived, rejoined, came back from another dimension, or the jukebox's chunk reloaded mid-song, which vanilla
 * resumes silently) starts it there, mid-song; one that's fallen behind catches up ({@link ClientJukeboxSync}). In
 * singleplayer, any menu that pauses the game also stops the jukebox's clock, so the music pauses with it, and the song,
 * its redstone and its comparator reading never drift apart.
 *
 * <p>Vanilla still starts and stops the song (its level events, heard {@link #RANGE} blocks away): a player without
 * Lifeguard hears jukeboxes as always, and is never sent ours ({@link Payloads}). A {@link Carrier} can carry a jukebox
 * further: listeners past vanilla's reach start it with the next heartbeat, already in sync, and are told when it stops.
 */
public final class JukeboxSync {
    /** How often a playing jukebox tells listeners where it is in its song, as vanilla's own play event. */
    public static final int HEARTBEAT_TICKS = 20;
    /** How far a jukebox is heard on its own, as in vanilla. */
    public static final int RANGE = 64;

    private static final List<Carrier> CARRIERS = new CopyOnWriteArrayList<>();

    /** Something that carries a jukebox's music past {@link #RANGE} (Supplementaries' speakers). */
    @FunctionalInterface
    public interface Carrier {
        /**
         * How many blocks further it carries the jukebox at {@code jukebox} right now (0: not at all). Asked once a
         * heartbeat, so it may show it doing so (speakers puff).
         */
        int carry(ServerLevel level, BlockPos jukebox);
    }

    private JukeboxSync() {
    }

    /** Registered once, at startup, by the entry that carries; it checks its own switch each time. */
    public static void addCarrier(Carrier carrier) {
        CARRIERS.add(carrier);
    }

    /**
     * The jukebox at {@code pos} is {@code elapsed} ticks into {@code song}: tells everyone in earshot; returns how far
     * it's heard now. Sent as far as it was heard last time too ({@code lastRange}): a speaker taken away, those it no
     * longer reaches hear it fade out.
     */
    public static int heartbeat(ServerLevel level, BlockPos pos, Holder<JukeboxSong> song, long elapsed, int lastRange) {
        int range = RANGE;
        for (Carrier carrier : CARRIERS)
            range += carrier.carry(level, pos);
        int id = level.registryAccess().registryOrThrow(Registries.JUKEBOX_SONG).getId(song.value());
        send(level, pos, Math.max(range, lastRange), new JukeboxSyncPayload(pos, id, elapsed, range));
        return range;
    }

    /** The jukebox at {@code pos} stopped. Vanilla tells those within {@link #RANGE}; this, the rest it reached. */
    public static void stop(ServerLevel level, BlockPos pos, int range) {
        if (range > RANGE)
            send(level, pos, range, new JukeboxStopPayload(pos));
    }

    private static void send(ServerLevel level, BlockPos pos, int radius, CustomPacketPayload payload) {
        for (ServerPlayer player : level.players())
            if (player.distanceToSqr(pos.getCenter()) <= (double) radius * radius)
                Payloads.send(player, payload);
    }
}
