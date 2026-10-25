package net.xlea99.lifeguard.mixin.cc_golden_bucket_compat.amendments;

import com.teamabnormals.caverns_and_chasms.common.item.GoldenBucketItem;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.events.behaviors.CauldronConversion;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.xlea99.lifeguard.entry.cc_golden_bucket_compat.GoldenBuckets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A filled golden bucket used on an empty cauldron, for a liquid Caverns & Chasms doesn't pour itself (milk, with
 * Neapolitan's milk cauldron off; Alex's Caves' purple soda; ...), becomes an Amendments cauldron holding a bucket of it,
 * the way a plain bucket of it does. Without this the click falls through to the bucket, and a golden milk bucket is
 * drunk.
 */
@Mixin(AbstractCauldronBlock.class)
public abstract class AbstractCauldronBlockMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void lifeguard$pourIntoEmpty(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                         InteractionHand hand, BlockHitResult hit,
                                         CallbackInfoReturnable<ItemInteractionResult> cir) {
        // Caverns & Chasms pours its own (water, lava, powder snow) into vanilla's cauldrons.
        if (!state.is(Blocks.CAULDRON) || !GoldenBuckets.isGoldenBucket(stack.getItem())
            || CauldronInteraction.EMPTY.map().containsKey(stack.getItem()) || !CommonConfigs.LIQUID_CAULDRON.get())
            return;
        SoftFluidStack held = GoldenBuckets.liquidOf(stack.getItem(), level.registryAccess());
        if (held == null)
            return;
        if (!level.isClientSide) {
            CauldronConversion.setCorrectCauldronStateAndTile(state, level, pos, held);
            if (level.getBlockState(pos) == state)
                return;
            player.setItemInHand(hand, GoldenBucketItem.getEmptySuccessItem(stack, player));
            GoldenBuckets.play(level, pos, stack.getItem(), false);
            level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
        }
        cir.setReturnValue(ItemInteractionResult.sidedSuccess(level.isClientSide));
    }
}
