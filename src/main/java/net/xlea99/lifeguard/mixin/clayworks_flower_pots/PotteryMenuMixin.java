package net.xlea99.lifeguard.mixin.clayworks_flower_pots;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.teamabnormals.clayworks.common.DecoratedPotTrimPattern;
import com.teamabnormals.clayworks.common.inventory.PotteryMenu;
import com.teamabnormals.clayworks.core.registry.ClayworksBlocks;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.ClayworksFlowerPots;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.FlowerPots;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Clayworks' pottery table dyes and etches flower pots. A decorated pot is dyed by becoming that color's pot block; a
 * flower pot stays a flower pot and takes the color as {@code base_color}. Each kind lists only its own designs:
 * Clayworks' are drawn for a decorated pot's face, ours for a flower pot's side ({@link FlowerPots#DESIGNS}).
 */
@Mixin(PotteryMenu.class)
public abstract class PotteryMenuMixin {

    @Shadow
    @Final
    Slot decoratedPotSlot;

    @Shadow
    @Final
    Slot dyeSlot;

    private boolean lifeguard$flowerPot() {
        return ClayworksFlowerPots.ENTRY.active() && FlowerPots.isFlowerPot(this.decoratedPotSlot.getItem());
    }

    @ModifyExpressionValue(method = "quickMoveStack",
        at = @At(value = "CONSTANT", args = "classValue=net/minecraft/world/level/block/DecoratedPotBlock"))
    private boolean lifeguard$shiftClickFlowerPots(boolean decoratedPot, @Local(ordinal = 1) ItemStack moved) {
        return decoratedPot || ClayworksFlowerPots.ENTRY.active() && FlowerPots.isFlowerPot(moved);
    }

    @ModifyReturnValue(method = "getSelectablePatterns(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", at = @At("RETURN"))
    private List<Holder<DecoratedPotTrimPattern>> lifeguard$ownDesigns(List<Holder<DecoratedPotTrimPattern>> patterns,
                                                                      @Local(argsOnly = true) ItemStack pot) {
        if (FlowerPots.isFlowerPot(pot))
            return ClayworksFlowerPots.DESIGNS.active()
                ? patterns.stream().filter(pattern -> pattern.is(FlowerPots.DESIGNS)).toList() : List.of();
        return patterns.stream().filter(pattern -> !pattern.is(FlowerPots.DESIGNS)).toList();
    }

    // Clayworks looks up the dyed decorated pot block before swapping to it; a flower pot has none (and a modded dye
    // color would throw there).
    @WrapOperation(method = "getResultStack",
        at = @At(value = "INVOKE", target = "Lcom/teamabnormals/clayworks/core/registry/ClayworksBlocks;getPotFromDyeColor(Lnet/minecraft/world/item/DyeColor;)Lnet/minecraft/world/level/block/Block;"))
    private Block lifeguard$noDecoratedPotForFlowerPots(DyeColor color, Operation<Block> original) {
        return this.lifeguard$flowerPot() ? Blocks.FLOWER_POT : original.call(color);
    }

    @WrapOperation(method = "getResultStack",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;transmuteCopy(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack lifeguard$dyeFlowerPot(ItemStack pot, ItemLike dyedPot, Operation<ItemStack> original) {
        if (!this.lifeguard$flowerPot() || !(this.dyeSlot.getItem().getItem() instanceof DyeItem dye))
            return original.call(pot, dyedPot);
        ItemStack dyed = pot.copy();
        dyed.set(DataComponents.BASE_COLOR, dye.getDyeColor());
        return dyed;
    }
}
