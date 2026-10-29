package net.xlea99.lifeguard.mixin.dye_depot_rock_candy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.xlea99.lifeguard.entry.dye_depot_rock_candy.RockCandies;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Confection Oven reads a Dye Depot rock candy's color ({@link RockCandies}).
 *
 * <p>The rock candy under a powered oven sets its gingerbread army's team color (that dye's text color, tinting their
 * icing; same color = allies). Alex's Caves checks the candy against its own colors one by one, so ours fell through to
 * a random color, a different team every time. When its lookup finds nothing, this looks at the same block it did (behind
 * an oven facing up or down, else under it) and answers for ours. Targeted by name: Lifeguard isn't built against Alex's
 * Caves.
 */
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.block.blockentity.ConfectionOvenBlockEntity")
public abstract class ConfectionOvenBlockEntityMixin {

    @Inject(method = "getDyeColorFromRockCandy", at = @At("RETURN"), cancellable = true)
    private static void lifeguard$dyeDepotCandy(Level level, BlockPos pos, BlockState oven, CallbackInfoReturnable<DyeColor> cir) {
        if (cir.getReturnValue() != null)
            return;
        Direction facing = oven.getBlock().getStateDefinition().getProperty("facing") instanceof Property<?> property
            && oven.getValue(property) instanceof Direction direction ? direction : Direction.NORTH;
        BlockPos candy = facing == Direction.UP ? pos.south() : facing == Direction.DOWN ? pos.north() : pos.below();
        DyeColor color = RockCandies.colorOf(level.getBlockState(candy).getBlock());
        if (color != null)
            cir.setReturnValue(color);
    }
}
