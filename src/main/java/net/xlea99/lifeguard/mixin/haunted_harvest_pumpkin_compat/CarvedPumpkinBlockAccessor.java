package net.xlea99.lifeguard.mixin.haunted_harvest_pumpkin_compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CarvedPumpkinBlock.class)
public interface CarvedPumpkinBlockAccessor {

    @Invoker("trySpawnGolem")
    void lifeguard$trySpawnGolem(Level level, BlockPos pos);
}
