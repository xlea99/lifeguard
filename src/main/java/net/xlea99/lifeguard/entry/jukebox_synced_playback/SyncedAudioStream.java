package net.xlea99.lifeguard.entry.jukebox_synced_playback;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.sounds.AudioStream;
import org.jetbrains.annotations.Nullable;

/**
 * A song's audio, for {@link SyncedJukeboxSound}: notes when the audio runs out (a song whose audio is shorter than the
 * length its mod gave it ends before its jukebox thinks it has, and mustn't be started again), and can start partway in.
 * Ogg streams can't seek, so the start is decoded and thrown away; decoding is far faster than playing, and whatever time
 * it takes is skipped too, so the stream starts where the song is by then.
 */
final class SyncedAudioStream implements AudioStream {
    private static final int CHUNK = 1 << 18;
    private static final ByteBuffer EMPTY = ByteBuffer.allocateDirect(0);

    private final AudioStream stream;
    private final SyncedJukeboxSound sound;
    /** What's left of the last buffer the skip read into. */
    @Nullable
    private ByteBuffer rest;

    private SyncedAudioStream(AudioStream stream, SyncedJukeboxSound sound) {
        this.stream = stream;
        this.sound = sound;
    }

    static AudioStream of(AudioStream stream, SyncedJukeboxSound sound) {
        return new SyncedAudioStream(stream, sound);
    }

    /** {@code stream}, started where {@code sound} should be by now (asked again as it skips: the song moves on). */
    static AudioStream skipped(AudioStream stream, SyncedJukeboxSound sound) {
        SyncedAudioStream synced = new SyncedAudioStream(stream, sound);
        AudioFormat format = stream.getFormat();
        int frame = Math.max(1, format.getChannels() * format.getSampleSizeInBits() / 8);
        double bytesPerSecond = format.getSampleRate() * frame;
        long skipped = 0;
        try {
            while (!sound.cancelled) {
                long target = (long) (sound.secondsIn() * bytesPerSecond);
                target -= target % frame;
                long left = target - skipped;
                if (left <= 0)
                    break;
                ByteBuffer buffer = stream.read((int) Math.min(left, CHUNK));
                if (buffer == null || !buffer.hasRemaining()) {
                    sound.audioEnded = true;
                    break;
                }
                if (buffer.remaining() > left) {
                    buffer.position(buffer.position() + (int) left);
                    synced.rest = buffer;
                    break;
                }
                skipped += buffer.remaining();
            }
        } catch (IOException e) {
            closeQuietly(stream);
            throw new UncheckedIOException(e);
        }
        // Stopped while it skipped: nothing will play it now, or close it.
        if (sound.cancelled)
            closeQuietly(stream);
        return synced;
    }

    @Override
    public AudioFormat getFormat() {
        return this.stream.getFormat();
    }

    @Override
    public ByteBuffer read(int size) throws IOException {
        if (this.sound.cancelled)
            return EMPTY;
        ByteBuffer buffer = this.rest != null ? this.rest : this.stream.read(size);
        this.rest = null;
        if (buffer == null || !buffer.hasRemaining())
            this.sound.audioEnded = true;
        return buffer;
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }

    private static void closeQuietly(AudioStream stream) {
        try {
            stream.close();
        } catch (IOException ignored) {
        }
    }
}
