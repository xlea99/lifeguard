package net.xlea99.lifeguard.mixin.cauldron_fourths;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * A bucket of powder snow is four units in Moonlight, like every other bucket: Moonlight lists powder snow buckets
 * (vanilla's, and other mods' like Caverns & Chasms' golden one) as three, matching vanilla's old three-level cauldron.
 * Changed as Moonlight reads its liquids, rather than by overriding its data file: a data override that switches off
 * with the entry would hide Moonlight's own file instead of falling back to it.
 */
@Mixin(FluidContainerList.Category.class)
public abstract class FluidContainerListCategoryMixin {

    @ModifyVariable(method = "decode(Lnet/minecraft/world/item/Item;ILjava/util/List;Ljava/util/Optional;Ljava/util/Optional;)Lnet/mehvahdjukaar/moonlight/api/fluids/FluidContainerList$Category;",
        at = @At("HEAD"), argsOnly = true)
    private static int lifeguard$powderSnowBucketIsFour(int capacity, @Local(argsOnly = true) List<Item> filled) {
        if (capacity != 3)
            return capacity;
        for (Item item : filled)
            if (BuiltInRegistries.ITEM.getKey(item).getPath().endsWith("powder_snow_bucket"))
                return 4;
        return capacity;
    }
}
