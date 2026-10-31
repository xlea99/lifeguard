package net.xlea99.lifeguard.entry.jukebox_synced_playback;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * A jukebox's song, as vanilla's ({@code SimpleSoundInstance.forJukeboxSong}), that can start partway in and knows how
 * far into it it is ({@link ClientJukeboxSync}).
 */
public class SyncedJukeboxSound extends SimpleSoundInstance {
    /** Songs still skipping to where they start; one that's stopped meanwhile is {@link #cancelled}. */
    static final Set<SyncedJukeboxSound> SKIPPING = ConcurrentHashMap.newKeySet();

    /** Where the next song started should start (ticks) and how far it's carried; set just before vanilla makes it. */
    private static long nextOffset;
    private static int nextExtra;

    /** Where in the song it started, in ticks. */
    final long offset;
    /** How far past its own range it was carried when it started (its jukebox's speakers). */
    final int extra;
    private final long createdAt = Util.getNanos();
    /** Ticks it's been playing (the game unpaused) since it started. */
    int playedTicks;
    /** Ticks since its jukebox last said it's still playing; {@link #heard} once it has at all. */
    int silentTicks;
    boolean heard;
    /** Its audio ran out (as opposed to being stopped). */
    volatile boolean audioEnded;
    /** Its audio's length in seconds, for a song that isn't streamed (0: streamed, {@link #audioEnded} tells). */
    volatile float duration;
    volatile boolean cancelled;

    private SyncedJukeboxSound(SoundEvent sound, Vec3 pos, long offset, int extra) {
        super(sound.getLocation(), SoundSource.RECORDS, 4.0F, 1.0F, SoundInstance.createUnseededRandom(), false, 0,
            Attenuation.LINEAR, pos.x, pos.y, pos.z, false);
        this.offset = offset;
        this.extra = extra;
    }

    public static SimpleSoundInstance create(SoundEvent sound, Vec3 pos) {
        SyncedJukeboxSound synced = new SyncedJukeboxSound(sound, pos, nextOffset, nextExtra);
        nextOffset = 0;
        nextExtra = 0;
        return synced;
    }

    static void startNextAt(long offset, int extra) {
        nextOffset = offset;
        nextExtra = extra;
    }

    /** How far into its song it is, in ticks. */
    long position() {
        return this.offset + this.playedTicks;
    }

    /** Played to the end of its audio. */
    boolean ended() {
        return this.audioEnded || this.duration > 0 && this.position() >= this.duration * 20 - 10;
    }

    void heard() {
        this.heard = true;
        this.silentTicks = 0;
    }

    /** Where it should be by now, in seconds: its offset, plus however long it's taken to load. */
    double secondsIn() {
        return this.offset / 20.0 + (Util.getNanos() - this.createdAt) / 1.0E9;
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary buffers, Sound sound, boolean looping) {
        CompletableFuture<AudioStream> stream = super.getStream(buffers, sound, looping);
        if (this.offset <= 0)
            return stream.thenApply(audio -> SyncedAudioStream.of(audio, this));
        // Skipping in means decoding everything before it: off the main thread, like the loading itself.
        SKIPPING.add(this);
        return stream.thenApplyAsync(audio -> {
            try {
                return SyncedAudioStream.skipped(audio, this);
            } finally {
                SKIPPING.remove(this);
            }
        }, Util.backgroundExecutor());
    }
}
