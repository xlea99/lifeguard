package net.xlea99.lifeguard.entry.jukebox_synced_playback;

import com.mojang.blaze3d.audio.Channel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent;
import net.neoforged.neoforge.client.event.sound.PlayStreamingSourceEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.xlea99.lifeguard.mixin.jukebox_synced_playback.ChannelAccessor;
import net.xlea99.lifeguard.mixin.jukebox_synced_playback.LevelRendererAccessor;
import net.xlea99.lifeguard.mixin.jukebox_synced_playback.SoundEngineAccessor;
import net.xlea99.lifeguard.mixin.jukebox_synced_playback.SoundManagerAccessor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

/**
 * The client's side of {@link JukeboxSync}: jukebox music follows the jukebox.
 * <ul>
 * <li>Its jukebox says it's playing and we aren't (just arrived, rejoined, back from another dimension, or our song was
 * cut off): start it there, mid-song. Unless we already played this song to the end of its audio here: a song whose
 * audio is shorter than the length its mod gave it ends before its jukebox thinks it has, and starting it again would
 * only play silence (and pop "Now Playing") every second.</li>
 * <li>We've fallen behind it: catch up. Ahead of it is left alone (the server running slow; jumping back would sound
 * worse than a late ending), unless by so much that it must have started the song over.</li>
 * <li>Its jukebox, close by, has gone quiet: stop.</li>
 * <li>The game is paused: pause. In singleplayer, any menu that pauses the game stops the jukebox too.</li>
 * </ul>
 * A song another mod plays its own way (Quark's ambience discs loop a plain sound of their own) only needs to be playing.
 */
public final class ClientJukeboxSync {
    /** A song more than this far behind its jukebox (ticks) starts again where the jukebox is. */
    private static final int CATCH_UP_TICKS = 40;
    /** A song this far ahead of its jukebox (ticks) means the jukebox started the song over. */
    private static final int STARTED_OVER_TICKS = 200;
    /** Within {@link #QUIET_RANGE} blocks of its jukebox, a song not heard from in this long (ticks) has stopped. */
    private static final int QUIET_TICKS = 100;
    private static final double QUIET_RANGE = 48;

    /** The song each jukebox last played here, and how far: it outlives the sound, to know one that already ended. */
    private static final Map<BlockPos, Track> TRACKS = new HashMap<>();
    /** How far past its own range each song is carried now, once that's changed since it started. */
    private static final Map<SoundInstance, Integer> EXTRA = new WeakHashMap<>();
    @Nullable
    private static ClientLevel tracksLevel;
    private static boolean paused;

    private record Track(ResourceLocation sound, long position, boolean ended) {
    }

    private ClientJukeboxSync() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ClientJukeboxSync::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientJukeboxSync::onPlayStreaming);
        NeoForge.EVENT_BUS.addListener(ClientJukeboxSync::onPlayStatic);
    }

    /** The jukebox at {@code pos} is playing {@code songId}, {@code elapsed} ticks in, heard {@code range} blocks away. */
    static void onSync(BlockPos pos, int songId, long elapsed, int range) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !JukeboxSyncedPlayback.ENTRY.active())
            return;
        Optional<Holder.Reference<JukeboxSong>> found = mc.level.registryAccess().registryOrThrow(Registries.JUKEBOX_SONG)
            .getHolder(songId);
        if (found.isEmpty())
            return;
        JukeboxSong song = found.get().value();
        int extra = Math.max(0, range - JukeboxSync.RANGE);
        ResourceLocation sound = song.soundEvent().value().getLocation();
        SoundInstance playing = playing(mc).get(pos);
        if (playing != null && playing.getLocation().equals(sound) && mc.getSoundManager().isActive(playing)) {
            carry(mc, playing, extra);
            if (!(playing instanceof SyncedJukeboxSound synced))
                return;
            synced.heard();
            long behind = elapsed - synced.position();
            if (behind <= CATCH_UP_TICKS && behind >= -STARTED_OVER_TICKS)
                return;
        } else {
            // The song still there (it may have ended between heartbeats), or the last one seen.
            Track track = playing instanceof SyncedJukeboxSound synced && synced.getLocation().equals(sound)
                ? new Track(sound, synced.position(), synced.ended()) : TRACKS.get(pos);
            if (track != null && track.ended() && track.sound().equals(sound)
                && elapsed >= track.position() - Math.min(STARTED_OVER_TICKS, track.position() / 2))
                return;
        }
        // Not worth starting for the last second of it.
        if (elapsed >= song.lengthInTicks() - 20)
            return;
        if (mc.options.getSoundSourceVolume(SoundSource.MASTER) <= 0 || mc.options.getSoundSourceVolume(SoundSource.RECORDS) <= 0)
            return;
        SyncedJukeboxSound.startNextAt(elapsed, extra);
        // Vanilla's own way in, so "Now Playing", dancing parrots and other mods' songs (Quark's ambience) all happen.
        mc.levelRenderer.playJukeboxSong(found.get(), pos);
        SyncedJukeboxSound.startNextAt(0, 0);
        if (playing(mc).get(pos) instanceof SyncedJukeboxSound synced)
            synced.heard();
    }

    /** The jukebox at {@code pos} stopped (we're past where vanilla's own stop reaches: it was carried to us). */
    static void onStop(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null)
            mc.levelRenderer.stopJukeboxSongAndNotifyNearby(pos);
    }

    /**
     * Carries {@code sound} {@code extra} blocks further than it's heard on its own (its jukebox's speakers), once it's
     * playing. Another mod's own sound for a song (Quark's ambience) is carried from here too, from its first heartbeat.
     */
    private static void carry(Minecraft mc, SoundInstance sound, int extra) {
        int now = EXTRA.getOrDefault(sound, sound instanceof SyncedJukeboxSound synced ? synced.extra : 0);
        if (now == extra || sound.getSound() == null)
            return;
        EXTRA.put(sound, extra);
        float range = ownRange(sound) + extra;
        onChannel(mc, sound, channel -> channel.linearAttenuation(range));
    }

    /** How far a sound is heard on its own, as vanilla works it out when it starts playing. */
    private static float ownRange(SoundInstance sound) {
        return Math.max(sound.getVolume(), 1.0F) * sound.getSound().getAttenuationDistance();
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != tracksLevel) {
            TRACKS.clear();
            tracksLevel = mc.level;
        }
        if (mc.level == null || mc.player == null || !JukeboxSyncedPlayback.ENTRY.active()) {
            paused = false;
            return;
        }
        Map<BlockPos, SoundInstance> playing = playing(mc);
        for (SyncedJukeboxSound sound : SyncedJukeboxSound.SKIPPING)
            if (!playing.containsValue(sound))
                sound.cancelled = true;
        boolean nowPaused = mc.isPaused();
        if (nowPaused != paused) {
            paused = nowPaused;
            for (SoundInstance sound : playing.values())
                onChannel(mc, sound, nowPaused ? Channel::pause : Channel::unpause);
        }
        if (nowPaused)
            return;
        List<BlockPos> quiet = new ArrayList<>();
        for (Map.Entry<BlockPos, SoundInstance> entry : playing.entrySet()) {
            if (!(entry.getValue() instanceof SyncedJukeboxSound synced) || !mc.getSoundManager().isActive(synced))
                continue;
            synced.playedTicks++;
            TRACKS.put(entry.getKey(), new Track(synced.getLocation(), synced.position(), synced.ended()));
            if (synced.heard && ++synced.silentTicks > QUIET_TICKS
                && mc.player.distanceToSqr(Vec3.atCenterOf(entry.getKey())) < QUIET_RANGE * QUIET_RANGE)
                quiet.add(entry.getKey());
        }
        for (BlockPos pos : quiet)
            mc.levelRenderer.stopJukeboxSongAndNotifyNearby(pos);
    }

    /** A song starts carried by its jukebox's speakers, and paused if it finished loading while the game was. */
    private static void onPlayStreaming(PlayStreamingSourceEvent event) {
        if (!(event.getSound() instanceof SyncedJukeboxSound synced))
            return;
        if (synced.extra > 0)
            event.getChannel().linearAttenuation(ownRange(synced) + synced.extra);
        if (Minecraft.getInstance().isPaused())
            event.getChannel().pause();
    }

    /**
     * A song that isn't streamed (some modded ones): its length is read off its buffer (no stream to say when it ran
     * out), and it starts partway in by jumping there.
     */
    private static void onPlayStatic(PlaySoundSourceEvent event) {
        if (!(event.getSound() instanceof SyncedJukeboxSound synced))
            return;
        int source = ((ChannelAccessor) event.getChannel()).lifeguard$source();
        int buffer = AL10.alGetSourcei(source, AL10.AL_BUFFER);
        int bytesPerSecond = AL10.alGetBufferi(buffer, AL10.AL_FREQUENCY) * AL10.alGetBufferi(buffer, AL10.AL_CHANNELS)
            * AL10.alGetBufferi(buffer, AL10.AL_BITS) / 8;
        if (bytesPerSecond > 0)
            synced.duration = (float) AL10.alGetBufferi(buffer, AL10.AL_SIZE) / bytesPerSecond;
        if (synced.offset > 0) {
            double seconds = synced.secondsIn();
            if (synced.duration > 0 && seconds >= synced.duration) {
                synced.audioEnded = true;
                event.getChannel().stop();
                return;
            }
            AL10.alSourcef(source, AL11.AL_SEC_OFFSET, (float) seconds);
        }
        if (synced.extra > 0)
            event.getChannel().linearAttenuation(ownRange(synced) + synced.extra);
        if (Minecraft.getInstance().isPaused())
            event.getChannel().pause();
    }

    private static Map<BlockPos, SoundInstance> playing(Minecraft mc) {
        return ((LevelRendererAccessor) mc.levelRenderer).lifeguard$playingJukeboxSongs();
    }

    private static void onChannel(Minecraft mc, SoundInstance sound, Consumer<Channel> action) {
        ChannelAccess.ChannelHandle handle = ((SoundEngineAccessor) ((SoundManagerAccessor) mc.getSoundManager())
            .lifeguard$soundEngine()).lifeguard$instanceToChannel().get(sound);
        if (handle != null)
            handle.execute(action);
    }
}
