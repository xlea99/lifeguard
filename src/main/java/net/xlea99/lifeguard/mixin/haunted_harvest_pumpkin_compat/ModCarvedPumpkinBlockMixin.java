package net.xlea99.lifeguard.mixin.haunted_harvest_pumpkin_compat;

import net.mehvahdjukaar.hauntedharvest.blocks.ModCarvedPumpkinBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.haunted_harvest_pumpkin_compat.Pumpkins;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A Haunted Harvest pumpkin that didn't make one of Haunted Harvest's golems gets vanilla's golem building too
 * ({@link Pumpkins}): Haunted Harvest builds its golems itself, so other mods' golems, hooked into vanilla's (Vanilla
 * Backport's copper golem), never got a look.
 */
@Mixin(ModCarvedPumpkinBlock.class)
public abstract class ModCarvedPumpkinBlockMixin {

    @Inject(method = "trySpawnGolemWithCustomPumpkin", at = @At("TAIL"))
    private void lifeguard$otherGolemsToo(Level level, BlockPos pos, CallbackInfo ci) {
        // Still here: Haunted Harvest's own golems take the pumpkin with them.
        if (level.getBlockState(pos).getBlock() == (Object) this)
            ((CarvedPumpkinBlockAccessor) this).lifeguard$trySpawnGolem(level, pos);
    }
}
