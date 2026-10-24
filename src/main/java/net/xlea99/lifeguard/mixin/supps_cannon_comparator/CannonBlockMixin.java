package net.xlea99.lifeguard.mixin.supps_cannon_comparator;

import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * A comparator reads how much gunpowder a cannon holds again. The signal is +15 while firing, +8 with a shot loaded,
 * +1 per gunpowder up to 4, then up to +3 more as the rest of the stack fills; but Supplementaries 4.0.2 scales that
 * last part by the stack's maximum size instead of its count, so it always adds 3: an empty cannon reads 3, and 5 to 64
 * gunpowder read the same. This feeds it the gunpowder past the first 4.
 */
@Mixin(CannonBlock.class)
public abstract class CannonBlockMixin {

    @ModifyArg(method = "getAnalogOutputSignal",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;map(FFFFF)F"), index = 0)
    private float lifeguard$gunpowderPastTheFirstFour(float stackSpace, @Local(argsOnly = true) Level level,
                                                    @Local(argsOnly = true) BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CannonBlockTile cannon ? Math.max(0, cannon.getFuel().getCount() - 4) : 0;
    }
}
