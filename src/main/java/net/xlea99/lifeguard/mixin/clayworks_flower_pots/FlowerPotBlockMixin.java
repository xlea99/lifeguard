package net.xlea99.lifeguard.mixin.clayworks_flower_pots;

import com.teamabnormals.clayworks.common.DecoratedPotTrim;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.ClayworksFlowerPots;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.FlowerPotBlockEntity;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.FlowerPots;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.PendingPot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives every flower pot a block entity holding its color and design, and keeps them through the three things that
 * would lose them: swapping to another pot (plant in, plant out), breaking, and pick-block. Applies to every subclass,
 * which is every potted plant, vanilla's and mods'; a mod pot overriding one of these keeps its own.
 */
@Mixin(FlowerPotBlock.class)
public abstract class FlowerPotBlockMixin extends Block implements EntityBlock {

    @Shadow
    public abstract FlowerPotBlock getEmptyPot();

    protected FlowerPotBlockMixin(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FlowerPotBlockEntity(pos, state);
    }

    // Pot to a different pot at the same spot: leave the looks for the new one. Anything else is a real removal.
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && newState.getBlock() instanceof FlowerPotBlock
            && level.getBlockEntity(pos) instanceof FlowerPotBlockEntity pot && !pot.plain() && PendingPot.willTake(pos, newState))
            PendingPot.put(pos, pot.looks());
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // Loot tables don't know the block entity: the pot item gets its looks afterwards. The plant is left alone.
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof FlowerPotBlockEntity pot && !pot.plain())
            for (ItemStack drop : drops)
                if (drop.is(this.getEmptyPot().asItem()))
                    drop.applyComponents(pot.collectComponents());
        return drops;
    }

    // Clayworks adds a decorated pot's "Design" lines through its block; a flower pot's go through ours.
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        DecoratedPotTrim design = FlowerPots.designOf(stack);
        if (design != null && ClayworksFlowerPots.DESIGNS.active())
            design.addToTooltip(context, tooltip::add, flag);
    }

    // Pick-block on an empty pot gives the pot item; on a potted plant vanilla gives the plant, which has no looks.
    @Inject(method = "getCloneItemStack", at = @At("RETURN"), cancellable = true)
    private void lifeguard$cloneWithLooks(LevelReader level, BlockPos pos, BlockState state, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        if (stack.is(this.getEmptyPot().asItem()) && level.getBlockEntity(pos) instanceof FlowerPotBlockEntity pot && !pot.plain()) {
            ItemStack withLooks = stack.copy();
            withLooks.applyComponents(pot.collectComponents());
            cir.setReturnValue(withLooks);
        }
    }
}
