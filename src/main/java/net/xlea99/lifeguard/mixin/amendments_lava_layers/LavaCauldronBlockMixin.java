package net.xlea99.lifeguard.mixin.amendments_lava_layers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.LavaCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.xlea99.lifeguard.entry.amendments_lava_layers.LavaLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla's lava cauldron turns into an Amendments cauldron holding four layers of lava the moment it's placed
 * ({@link LavaLayers}). Catching it here covers everything that makes one, without knowing each: a lava bucket, a
 * dripping dripstone, Supplementaries' faucets, other mods' buckets, commands. One that already exists (placed before
 * this was on) turns over the first time something is used on it or falls into it.
 */
@Mixin(LavaCauldronBlock.class)
public abstract class LavaCauldronBlockMixin extends AbstractCauldronBlock {

    protected LavaCauldronBlockMixin(Properties properties, CauldronInteraction.InteractionMap interactions) {
        super(properties, interactions);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && LavaLayers.cauldronsOn())
            LavaLayers.toLavaLayers(level, pos, LavaLayers.FULL);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && LavaLayers.cauldronsOn()) {
            LavaLayers.toLavaLayers(level, pos, LavaLayers.FULL);
            return level.getBlockState(pos).useItemOn(stack, level, player, hand, hit);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void lifeguard$turnOver(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!level.isClientSide && LavaLayers.cauldronsOn()) {
            LavaLayers.toLavaLayers(level, pos, LavaLayers.FULL);
            ci.cancel();
        }
    }
}
