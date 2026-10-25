package net.xlea99.lifeguard.mixin.cc_golden_bucket_compat;

import java.util.List;
import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.minecraft.world.item.Item;
import net.xlea99.lifeguard.entry.cc_golden_bucket_compat.GoldenBuckets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Golden buckets come off Moonlight's lists of containers as it reads its liquids: listed as plain one-bucket
 * containers, emptying one into a tank handed back a plain bucket, losing the golden one and any buckets still in it.
 * Golden buckets are handled whole instead ({@link GoldenBuckets}).
 */
@Mixin(FluidContainerList.Category.class)
public abstract class FluidContainerListCategoryMixin {

    @ModifyVariable(method = "decode(Lnet/minecraft/world/item/Item;ILjava/util/List;Ljava/util/Optional;Ljava/util/Optional;)Lnet/mehvahdjukaar/moonlight/api/fluids/FluidContainerList$Category;",
        at = @At("HEAD"), argsOnly = true)
    private static List<Item> lifeguard$noGoldenBuckets(List<Item> filled) {
        return filled.stream().filter(item -> !GoldenBuckets.isGoldenBucket(item)).toList();
    }
}
