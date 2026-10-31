package net.xlea99.lifeguard.mixin.species_coil_culling;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * A stretched Species Coil is culled as the whole line, not as its knot.
 *
 * <p>The line is drawn by its end coil, from that coil to the start, sagging by a tenth of its length per looseness
 * step. The coil's renderer asks to always be drawn, but culling mods judge an entity by its culling box after that, and
 * Coil's was its own quarter-block knot: Entity Culling hid the line whenever the knot was out of sight (it sits against
 * a block, so from many angles), and Sodium whenever the knot's chunk section wasn't visible, as when looking at the
 * middle of a long line. This box spans both ends and the sag. Only synced values are read (the start's block, not the
 * start entity), so it costs nothing to ask for. Written against our build of Species' 1.21.1 port (PR #88), by class
 * name.
 */
@Mixin(targets = "com.ninni.species.server.entity.mob.update_3.Coil")
public abstract class CoilMixin extends Entity {
    protected CoilMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow
    public abstract boolean isStartPoint();

    @Shadow
    public abstract int getLooseness();

    @Shadow
    public abstract BlockPos getEndPointPos();

    @Override
    public AABB getBoundingBoxForCulling() {
        AABB knot = this.getBoundingBox();
        BlockPos startBlock = getEndPointPos();
        if (isStartPoint() || startBlock == null)
            return knot;
        AABB start = new AABB(startBlock).inflate(0.5);
        double sag = knot.getCenter().distanceTo(Vec3.atCenterOf(startBlock)) * 0.1 * getLooseness();
        // The curve's low point is half the control point's drop; the rest is room for the rope's width.
        AABB line = knot.minmax(start);
        return line.setMinY(line.minY - sag / 2.0).inflate(0.5);
    }
}
