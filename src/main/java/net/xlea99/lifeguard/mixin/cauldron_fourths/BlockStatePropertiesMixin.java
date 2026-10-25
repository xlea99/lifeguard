package net.xlea99.lifeguard.mixin.cauldron_fourths;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The cauldron level, shared by every layered cauldron (water, powder snow, and other mods' cauldrons built the same
 * way, like Amendments' dye cauldron and Neapolitan's milk cauldrons), runs 1 to 4 instead of 1 to 3: a bucket is four
 * levels and a bottle one, the same unit as every Moonlight container. It's the one property object they all read, so
 * widening it here moves every one of them together.
 */
@Mixin(BlockStateProperties.class)
public abstract class BlockStatePropertiesMixin {

    @WrapOperation(method = "<clinit>",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/properties/IntegerProperty;create(Ljava/lang/String;II)Lnet/minecraft/world/level/block/state/properties/IntegerProperty;"))
    private static IntegerProperty lifeguard$fourLevels(String name, int min, int max, Operation<IntegerProperty> original) {
        // LEVEL_CAULDRON is the only "level" property from 1 to 3.
        if (name.equals("level") && min == 1 && max == 3)
            return original.call(name, min, 4);
        return original.call(name, min, max);
    }
}
