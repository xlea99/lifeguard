package net.xlea99.lifeguard.mixin.haunted_harvest_unified_carved_pumpkin;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin.CarvingPools;
import net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin.GeneratingRegion;
import net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin.UnifiedPumpkin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Worldgen places Haunted Harvest's pumpkins where it would place vanilla's, carved from the pool for the structure or
 * feature being generated ({@link UnifiedPumpkin}, {@link CarvingPools}): every structure and feature, any mod's, places
 * its blocks through here. The game already tracks what it's generating (for its crash reports). Every block worldgen
 * places passes here, so the vanilla-pumpkin check comes first.
 */
@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin implements GeneratingRegion {
    @Shadow
    @Nullable
    private Supplier<String> currentlyGenerating;

    @Override
    @Nullable
    public String lifeguard$generating() {
        return currentlyGenerating == null ? null : currentlyGenerating.get();
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"),
        cancellable = true)
    private void lifeguard$placeHauntedPumpkin(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (!UnifiedPumpkin.isVanilla(state) || !UnifiedPumpkin.active())
            return;
        WorldGenRegion self = (WorldGenRegion) (Object) this;
        boolean placed = self.setBlock(pos, UnifiedPumpkin.hauntedState(state), flags, recursionLeft);
        if (placed)
            UnifiedPumpkin.carve(self, pos, CarvingPools.forWorldgen(self, pos));
        cir.setReturnValue(placed);
    }
}
