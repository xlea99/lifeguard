package net.xlea99.lifeguard.mixin.twigs_cc_flowering_azaleas;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.xlea99.lifeguard.entry.twigs_cc_flowering_azaleas.FloweringAzaleaTrees;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A flowering azalea grown into a tree flowers its logs ({@link FloweringAzaleaTrees}). */
@Mixin(TreeGrower.class)
public abstract class TreeGrowerMixin {
    @Inject(method = "growTree", at = @At("RETURN"))
    private void lifeguard$floweringAzalea(ServerLevel level, ChunkGenerator generator, BlockPos pos, BlockState state,
        RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && state.is(Blocks.FLOWERING_AZALEA))
            FloweringAzaleaTrees.flower(level, pos, random);
    }
}
