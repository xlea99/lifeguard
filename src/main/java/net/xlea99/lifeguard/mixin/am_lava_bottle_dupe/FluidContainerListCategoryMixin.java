package net.xlea99.lifeguard.mixin.am_lava_bottle_dupe;

import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

/**
 * Alex's Mobs' Lava Bottles stop making lava out of nothing. Filling a glass bottle at lava never uses the lava up, and
 * Moonlight lists the Lava Bottle as a quarter-bucket container of lava, so every Moonlight-based container
 * (Supplementaries' jars and goblets, Amendments' cauldrons, faucets) turned four free bottles into a bucket. Here the
 * Lava Bottle is taken off that list as Moonlight reads its liquids, so nothing pours one in or fills one; it still
 * brews, and bottling lava still works.
 */
@Mixin(FluidContainerList.Category.class)
public abstract class FluidContainerListCategoryMixin {

    @Unique
    private static final ResourceLocation lifeguard$LAVA_BOTTLE =
        ResourceLocation.fromNamespaceAndPath("alexsmobs", "lava_bottle");

    @ModifyVariable(method = "decode(Lnet/minecraft/world/item/Item;ILjava/util/List;Ljava/util/Optional;Ljava/util/Optional;)Lnet/mehvahdjukaar/moonlight/api/fluids/FluidContainerList$Category;",
        at = @At("HEAD"), argsOnly = true)
    private static List<Item> lifeguard$noLavaBottles(List<Item> filled) {
        return filled.stream().filter(item -> !lifeguard$LAVA_BOTTLE.equals(BuiltInRegistries.ITEM.getKey(item))).toList();
    }
}
