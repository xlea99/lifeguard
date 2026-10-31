package net.xlea99.lifeguard.entry.supps_jukebox_speakers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.xlea99.lifeguard.entry.jukebox_synced_playback.JukeboxSync;

/**
 * Supplementaries' speaker blocks as a jukebox's speakers. A speaker touching a jukebox (any side, facing anywhere)
 * stops broadcasting messages and its menu becomes just its range, which adds to how far the jukebox is heard:
 * {@link JukeboxSync#RANGE} on its own, plus up to {@link #MAX_EACH} from each of up to {@link #MAX_SPEAKERS} speakers
 * (the biggest, with more). It doesn't make the music louder, only carry further. The speakers carrying it puff
 * together, in one color, with each heartbeat.
 */
public final class JukeboxSpeakers {
    public static final int MAX_SPEAKERS = 2;
    public static final int MAX_EACH = 64;

    private JukeboxSpeakers() {
    }

    /** {@link JukeboxSync.Carrier}: the range the jukebox's speakers add, and their puff. */
    public static int carry(ServerLevel level, BlockPos jukebox) {
        if (!SuppsJukeboxSpeakers.ENTRY.active())
            return 0;
        List<SpeakerBlockTile> speakers = new ArrayList<>();
        for (Direction side : Direction.values())
            if (level.getBlockEntity(jukebox.relative(side)) instanceof SpeakerBlockTile speaker)
                speakers.add(speaker);
        speakers.sort(Comparator.comparingDouble(SpeakerBlockTile::getVolume).reversed());
        int extra = 0;
        // One color for all of them, sent as 1 + the color (0 is the speaker's own puff, a random one).
        int color = 1 + level.random.nextInt(24);
        for (SpeakerBlockTile speaker : speakers.subList(0, Math.min(MAX_SPEAKERS, speakers.size()))) {
            extra += Math.max(0, Math.min(MAX_EACH, (int) Math.round(speaker.getVolume())));
            level.blockEvent(speaker.getBlockPos(), speaker.getBlockState().getBlock(), 0, color);
        }
        return extra;
    }

    /** True if the speaker at {@code speaker} is touching a jukebox (so it's that jukebox's speaker). */
    public static boolean linked(Level level, BlockPos speaker) {
        if (!SuppsJukeboxSpeakers.ENTRY.active())
            return false;
        for (Direction side : Direction.values())
            if (level.getBlockState(speaker.relative(side)).getBlock() instanceof JukeboxBlock)
                return true;
        return false;
    }
}
