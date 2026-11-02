package net.xlea99.lifeguard.entry.jukebox_record_scratch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.AABB;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.entry.jukebox_synced_playback.JukeboxSync;

/**
 * The record scratch. When a creeper's fuse catches (its hiss) within {@link #REACH} blocks of a playing jukebox, the
 * music cuts with a scratch and the jukebox's clock stops: the song keeps its place. Once no creeper near it is still
 * hissing (it blew, died, or walked off), {@link #AWKWARD_SILENCE} of nothing at all, then the song picks up from there,
 * started again mid-way by synced playback. (Without that silence the music came back the moment the creeper blew,
 * a fuse is only 1.5 seconds, and the bit barely landed.) A
 * player without Lifeguard hears the music stop (vanilla's own stop) but not the scratch or the restart.
 *
 * <p>The sound is luffy's "record scratch.wav" from Freesound (CC BY 4.0, credited in NOTICE.md), and it's sent by
 * name, so it needs no registry entry: a client without it simply hears nothing.
 */
public final class RecordScratch {
    /** How close the creeper must be to the jukebox: right by it. */
    static final int REACH = 5;
    /** A little further for "still hissing nearby", so a creeper at the edge doesn't flicker the music on and off. */
    private static final int HOLD_REACH = REACH + 2;
    /** Once nothing by it is hissing any more (blown up, killed, calmed down): this long of awkward silence (ticks). */
    public static final int AWKWARD_SILENCE = 100;
    private static final Holder<SoundEvent> SCRATCH = Holder.direct(SoundEvent.createVariableRangeEvent(
        ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "block.jukebox.record_scratch")));

    private RecordScratch() {
    }

    /** A creeper's fuse just caught: every playing jukebox right by it scratches to a stop. */
    public static void hiss(ServerLevel level, Creeper creeper) {
        BlockPos center = creeper.blockPosition();
        double reachSquared = (double) REACH * REACH;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-REACH, -REACH, -REACH), center.offset(REACH, REACH, REACH))) {
            if (pos.getCenter().distanceToSqr(creeper.position()) > reachSquared
                || !(level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox)
                || !jukebox.getSongPlayer().isPlaying()
                || !((ScratchedJukebox) jukebox.getSongPlayer()).lifeguard$scratch())
                continue;
            BlockPos at = pos.immutable();
            JukeboxSync.silence(level, at);
            level.playSound(null, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, SCRATCH, SoundSource.RECORDS, 1.5F, 1.0F);
        }
    }

    /** Whether a creeper by the jukebox at {@code pos} is still hissing: its song waits while one is. */
    public static boolean stillHissing(ServerLevel level, BlockPos pos) {
        return !level.getEntitiesOfClass(Creeper.class, new AABB(pos).inflate(HOLD_REACH),
            creeper -> creeper.isAlive() && creeper.getSwellDir() > 0).isEmpty();
    }
}
