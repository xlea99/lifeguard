package net.xlea99.lifeguard.mixin.cc_tether_potion_compat.amendments;

import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.events.behaviors.CauldronConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.xlea99.lifeguard.entry.cc_tether_potion_compat.RiggedPotions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A tether, impact or trail potion used on an empty cauldron breaks open into it, making an Amendments cauldron of its
 * potion, the way its bottle would (see {@link RiggedPotions}).
 */
@Mixin(AbstractCauldronBlock.class)
public abstract class AbstractCauldronBlockMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void lifeguard$breakOpenIntoEmpty(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit,
                                              CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (!state.is(Blocks.CAULDRON) || !CommonConfigs.LIQUID_CAULDRON.get())
            return;
        ItemStack potion = RiggedPotions.brokenOpen(stack);
        if (potion == null)
            return;
        BlockState filled = CauldronConversion.getNewState(pos, level, potion);
        if (filled == null)
            return;
        if (!level.isClientSide) {
            level.setBlockAndUpdate(pos, filled);
            if (!(level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile cauldron)) {
                level.setBlockAndUpdate(pos, state);
                return;
            }
            InteractionResultHolder<ItemStack> poured = cauldron.getSoftFluidTank().drainItem(potion, level, pos, false, false);
            if (!poured.getResult().consumesAction()) {
                level.setBlockAndUpdate(pos, state);
                return;
            }
            cauldron.setChanged();
            RiggedPotions.smashed(player, hand, stack, poured.getObject(), level, pos);
        }
        cir.setReturnValue(ItemInteractionResult.sidedSuccess(level.isClientSide));
    }
}
