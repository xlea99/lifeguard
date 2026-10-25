package net.xlea99.lifeguard.entry.amendments_lava_layers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import vectorwing.farmersdelight.common.block.entity.HeatableBlockEntity;
import vectorwing.farmersdelight.common.tag.ModTags;

/**
 * A cauldron of lava still heats Farmer's Delight's cooking pots and skillets with lava kept as layers
 * ({@link LavaLayers}). Farmer's Delight lists vanilla's lava cauldron as a heat source by block tag; an Amendments
 * cauldron can't be tagged that way (most of them hold water or soda), so its checks also ask this. Mirrors Farmer's
 * Delight's own rules: right below, or below a heat conductor unless the cooker needs direct heat; for the held
 * skillet, anywhere next to the player.
 */
public final class CookingHeat {

    private CookingHeat() {
    }

    public static boolean heatsFromBelow(HeatableBlockEntity cooker, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return LavaLayers.lava(level, below) != null || !cooker.requiresDirectHeat()
            && level.getBlockState(below).is(ModTags.Blocks.HEAT_CONDUCTORS) && LavaLayers.lava(level, below.below()) != null;
    }

    public static boolean nearPlayer(Player player, LevelReader level) {
        BlockPos pos = player.blockPosition();
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1)))
            if (LavaLayers.lava(level, nearby) != null)
                return true;
        return false;
    }
}
