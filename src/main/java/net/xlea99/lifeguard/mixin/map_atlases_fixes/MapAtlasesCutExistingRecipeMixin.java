package net.xlea99.lifeguard.mixin.map_atlases_fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.item.crafting.CraftingInput;
import net.xlea99.lifeguard.entry.map_atlases_fixes.AtlasFixes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import pepjebs.mapatlases.item.recipe.MapAtlasesCutExistingRecipe;
import pepjebs.mapatlases.map_collection.MapCollection;
import pepjebs.mapatlases.utils.AtlasMap;
import pepjebs.mapatlases.utils.Slice;

/** Cutting a map out of an atlas in a crafting grid removes the map the result showed ({@link AtlasFixes}). */
@Mixin(MapAtlasesCutExistingRecipe.class)
public abstract class MapAtlasesCutExistingRecipeMixin {
    private static final String PICK = "Lpepjebs/mapatlases/item/recipe/MapAtlasesCutExistingRecipe;getMapToRemove(Lnet/minecraft/world/item/crafting/CraftingInput;Lpepjebs/mapatlases/map_collection/MapCollection;Lpepjebs/mapatlases/utils/Slice;)Lpepjebs/mapatlases/utils/AtlasMap;";

    @WrapOperation(method = "assemble", at = @At(value = "INVOKE", target = PICK))
    private AtlasMap lifeguard$rememberShown(CraftingInput input, MapCollection maps, Slice slice, Operation<AtlasMap> original) {
        AtlasMap shown = original.call(input, maps, slice);
        AtlasFixes.rememberShownCut(input, shown);
        return shown;
    }

    @WrapOperation(method = "cutOneMap", at = @At(value = "INVOKE", target = PICK))
    private static AtlasMap lifeguard$cutTheShownOne(CraftingInput input, MapCollection maps, Slice slice, Operation<AtlasMap> original) {
        AtlasMap shown = AtlasFixes.shownCut(input, maps);
        return shown != null ? shown : original.call(input, maps, slice);
    }
}
