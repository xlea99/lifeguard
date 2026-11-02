package net.xlea99.lifeguard.mixin.enchanting_tables_keep_lapis;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.enchanting_tables_keep_lapis.TableLapis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The enchanting table doesn't override onRemove, so vanilla's runs: hooked at HEAD, before it removes the block entity
 * that holds the kept lapis. Not gated: a broken table always drops what it keeps.
 */
@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    @Inject(method = "onRemove", at = @At("HEAD"))
    private void lifeguard$dropKeptLapis(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston,
                                         CallbackInfo ci) {
        if ((Object) this instanceof EnchantingTableBlock && !state.is(newState.getBlock()))
            TableLapis.dropStored(level, pos);
    }
}
