package net.xlea99.lifeguard.mixin.cc_glare_grumpiness;

import com.teamabnormals.caverns_and_chasms.common.entity.animal.Glare;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.dimension.DimensionType;
import net.xlea99.lifeguard.entry.cc_glare_grumpiness.CcGlareGrumpiness;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A Glare is grumpy where monsters could spawn, and seeks those spots out (#42). Caverns & Chasms only checked the
 * block light (torches and the like), so in daylight, where the block light is 0, every Glare was grumpy. This adds the
 * rest of vanilla's monster light test: the light the place gets from the sky too, as dark as a monster needs (darker
 * skies in a thunderstorm count, as for monsters). Where monsters spawn whatever the block light (the Nether), it stays
 * grumpy, as Caverns & Chasms has it.
 */
@Mixin(Glare.class)
public abstract class GlareMixin {
    @Inject(method = "shouldBeGrumpy", at = @At("HEAD"), cancellable = true)
    private static void lifeguard$grumpyWhereMonstersSpawn(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!CcGlareGrumpiness.ENTRY.active())
            return;
        DimensionType dimension = level.dimensionType();
        int blockLightLimit = dimension.monsterSpawnBlockLightLimit();
        if (blockLightLimit >= 15) {
            cir.setReturnValue(true);
            return;
        }
        if (level.getBrightness(LightLayer.BLOCK, pos) > blockLightLimit) {
            cir.setReturnValue(false);
            return;
        }
        int light = level.isThundering() ? level.getMaxLocalRawBrightness(pos, 10) : level.getMaxLocalRawBrightness(pos);
        cir.setReturnValue(light <= dimension.monsterSpawnLightTest().getMaxValue());
    }
}
