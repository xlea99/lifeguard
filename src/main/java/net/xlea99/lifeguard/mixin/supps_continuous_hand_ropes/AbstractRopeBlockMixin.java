package net.xlea99.lifeguard.mixin.supps_continuous_hand_ropes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AbstractRopeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.xlea99.lifeguard.entry.supps_continuous_hand_ropes.HandRopes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Clicking a rope with a rope (let it down) or an empty hand (pull it up) moves what hangs from it like a pulley does
 * ({@link HandRopes}), instead of snapping one block. The rope is spent or handed back as before.
 */
@Mixin(AbstractRopeBlock.class)
public abstract class AbstractRopeBlockMixin {

    @WrapOperation(method = "useItemOn",
        at = @At(value = "INVOKE",
            target = "Lnet/mehvahdjukaar/supplementaries/common/misc/block_movement/InstantPulleyMover;addRopeDown(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean lifeguard$letDown(BlockPos start, Level level, Player player, InteractionHand hand, Block rope,
                                      Operation<Boolean> original) {
        return switch (HandRopes.click(level, start, rope, true)) {
            case MOVED -> true;
            case BUSY -> false;
            case NOT_MINE -> original.call(start, level, player, hand, rope);
        };
    }

    @WrapOperation(method = "useItemOn",
        at = @At(value = "INVOKE",
            target = "Lnet/mehvahdjukaar/supplementaries/common/misc/block_movement/InstantPulleyMover;removeRopeDown(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean lifeguard$pullUp(BlockPos start, Level level, Block rope, Operation<Boolean> original) {
        return switch (HandRopes.click(level, start, rope, false)) {
            case MOVED -> true;
            case BUSY -> false;
            case NOT_MINE -> original.call(start, level, rope);
        };
    }
}
