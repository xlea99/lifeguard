package net.xlea99.lifeguard.mixin.map_atlases_fixes;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.map_atlases_fixes.AtlasFixes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The cartography table's atlas results keep to the map limit and leave stacks alone ({@link AtlasFixes}). The targets
 * are the result methods Map Atlases' own mixin adds to the menu, hence the higher priority (applied after it).
 */
@Mixin(value = CartographyTableMenu.class, priority = 1500)
public abstract class CartographyTableMenuMixin {

    @Dynamic("Added by Map Atlases' CartographyTableMenuMixin")
    @Inject(method = "mapatlases$addFilledMap", at = @At("HEAD"), cancellable = true)
    private void lifeguard$noRoomForTheMap(ItemStack atlas, ItemStack map, Level level, CallbackInfoReturnable<ItemStack> cir) {
        if (AtlasFixes.isFull(atlas, level))
            cir.setReturnValue(null);
    }

    @Dynamic("Added by Map Atlases' CartographyTableMenuMixin")
    @ModifyReturnValue(method = "mapatlases$mergeAtlases", at = @At("RETURN"))
    private ItemStack lifeguard$mergeWithinLimit(ItemStack merged, @Local(argsOnly = true) Level level) {
        return merged != null && AtlasFixes.isOverLimit(merged, level) ? null : merged;
    }

    /** Shears cut one atlas at a time: with a stack in the slot, the map would leave every atlas in it. */
    @Dynamic("Added by Map Atlases' CartographyTableMenuMixin")
    @Inject(method = "mapatlases$cutSelectedMap", at = @At("HEAD"), cancellable = true)
    private void lifeguard$cutOneAtlas(ItemStack atlas, Level level, CallbackInfoReturnable<ItemStack> cir) {
        if (atlas.getCount() > 1)
            cir.setReturnValue(null);
    }
}
