package net.xlea99.lifeguard.mixin.neapolitan_amendments_milkshakes;

import net.mehvahdjukaar.amendments.common.block.ModCauldronBlock;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.xlea99.lifeguard.entry.neapolitan_amendments_milkshakes.Milkshakes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Ice cream on an Amendments cauldron of milk makes a Neapolitan milkshake cauldron (see {@link Milkshakes}). */
@Mixin(ModCauldronBlock.class)
public abstract class ModCauldronBlockMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void lifeguard$iceCreamIntoMilk(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit,
                                           CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (!(level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile cauldron))
            return;
        SoftFluidStack fluid = cauldron.getSoftFluidTank().getFluid();
        if (fluid.isEmpty() || !fluid.is(MLBuiltinSoftFluids.MILK))
            return;
        Block milkshake = Milkshakes.milkshakeCauldronFor(stack);
        if (milkshake != null)
            cir.setReturnValue(Milkshakes.makeMilkshake(milkshake, fluid.getCount(), stack, level, pos, player, hand));
    }
}
