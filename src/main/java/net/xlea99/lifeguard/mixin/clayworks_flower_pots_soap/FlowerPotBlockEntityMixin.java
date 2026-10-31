package net.xlea99.lifeguard.mixin.clayworks_flower_pots_soap;

import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.ClayworksFlowerPots;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.FlowerPotBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

/** Supplementaries' soap washes a flower pot's color off and leaves its design, as on a decorated pot. */
@Mixin(FlowerPotBlockEntity.class)
public abstract class FlowerPotBlockEntityMixin implements IWashable {

    @Override
    public boolean tryWash(Level level, BlockPos pos, BlockState state, Vec3 hit) {
        FlowerPotBlockEntity pot = (FlowerPotBlockEntity) (Object) this;
        if (!ClayworksFlowerPots.SOAP.active() || pot.color() == null)
            return false;
        if (!level.isClientSide)
            pot.setColor(null);
        return true;
    }
}
