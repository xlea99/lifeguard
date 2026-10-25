package net.xlea99.lifeguard.mixin.amendments_lava_layers;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.amendments.common.block.ModCauldronBlock;
import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.xlea99.lifeguard.entry.amendments_lava_layers.LavaLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * An Amendments cauldron holding lava does what vanilla's lava cauldron did ({@link LavaLayers}). Whatever falls in
 * burns, the way vanilla's burns mobs and thrown items (Amendments' own lava only keeps things from being put out). A
 * full one gives up its bucket of lava the vanilla way to anything vanilla knows how to use on a lava cauldron (other
 * mods' buckets Moonlight doesn't list). A full bucket of anything that doesn't fit isn't poured: nothing happens,
 * instead of the bucket emptying beside the cauldron.
 */
@Mixin(ModCauldronBlock.class)
public abstract class ModCauldronBlockMixin extends AbstractCauldronBlock {

    protected ModCauldronBlockMixin(Properties properties, CauldronInteraction.InteractionMap interactions) {
        super(properties, interactions);
    }

    @Inject(method = "entityInside", at = @At("TAIL"))
    private void lifeguard$lavaBurns(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (this.isEntityInsideContent(state, pos, entity) && LavaLayers.lava(level, pos) != null)
            entity.lavaHurt();
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void lifeguard$lavaCauldronInteractions(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                    Player player, InteractionHand hand, BlockHitResult hit,
                                                    CallbackInfoReturnable<ItemInteractionResult> cir) {
        SoftFluidTank lava = LavaLayers.lava(level, pos);
        if (lava == null)
            return;
        boolean full = lava.getFluid().getCount() >= LavaLayers.FULL;
        // A full bucket of anything that doesn't fit does nothing. Vanilla would empty a lava bucket into a full lava
        // cauldron (gone), and passing it on would let the bucket pour out beside the cauldron instead.
        Pair<SoftFluidStack, FluidContainerList.Category> held = SoftFluidStack.fromItem(stack, level.registryAccess());
        if (held != null && !held.getFirst().isEmpty()) {
            SoftFluidStack fluid = held.getFirst();
            if (fluid.getCount() >= LavaLayers.FULL && lava.addFluid(fluid.copy(), true) < fluid.getCount())
                cir.setReturnValue(ItemInteractionResult.CONSUME);
            return;
        }
        // Anything else vanilla knows how to use on a full lava cauldron does exactly that.
        CauldronInteraction interaction = full ? CauldronInteraction.LAVA.map().get(stack.getItem()) : null;
        if (interaction == null)
            return;
        ItemInteractionResult result = interaction.interact(Blocks.LAVA_CAULDRON.defaultBlockState(), level, pos, player,
            hand, stack);
        if (result.consumesAction())
            cir.setReturnValue(result);
    }
}
