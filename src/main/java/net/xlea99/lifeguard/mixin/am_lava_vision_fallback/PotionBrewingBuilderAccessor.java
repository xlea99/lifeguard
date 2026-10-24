package net.xlea99.lifeguard.mixin.am_lava_vision_fallback;

import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Reaches the brewing builder's own recipe list, to take out a recipe (see {@code LavaVision}). */
@Mixin(PotionBrewing.Builder.class)
public interface PotionBrewingBuilderAccessor {
    @Accessor("recipes")
    List<IBrewingRecipe> lifeguard$recipes();
}
