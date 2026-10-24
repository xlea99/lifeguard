package net.xlea99.lifeguard.mixin.enderman_head_reach;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import java.util.List;
import net.mehvahdjukaar.supplementaries.common.block.tiles.EndermanSkullBlockTile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.xlea99.lifeguard.entry.enderman_head_reach.EndermanHeadReach;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Longer reach for Supplementaries' enderman head, which powers redstone while a player stares at it.
 *
 * <p>{@code isBeingWatched} runs every server tick for every placed head: it collects the players in a box around the
 * head, then raytraces from each one to see if they're looking at it. The box's half-size is a hardcoded 20; it now comes
 * from the entry's range. The player search scanned every chunk section the box covers, so a bigger box would have cost
 * more for every head, every tick, whether or not anyone was near; it now filters the level's player list, which costs
 * the same at any range.
 */
@Mixin(EndermanSkullBlockTile.class)
public abstract class EndermanSkullBlockTileMixin {

    @ModifyConstant(method = "isBeingWatched", constant = @Constant(intValue = 20))
    private static int lifeguard$watchRange(int original) {
        return EndermanHeadReach.RANGE.get();
    }

    /** Same result as the box query it replaces: non-spectator players whose hitbox touches the box. */
    @WrapOperation(method = "isBeingWatched", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
    private static List<Player> lifeguard$playersNear(Level level, Class<Player> type, AABB box, Operation<List<Player>> original) {
        List<Player> found = new ArrayList<>(2);
        for (Player player : level.players())
            if (!player.isSpectator() && player.getBoundingBox().intersects(box))
                found.add(player);
        return found;
    }
}
