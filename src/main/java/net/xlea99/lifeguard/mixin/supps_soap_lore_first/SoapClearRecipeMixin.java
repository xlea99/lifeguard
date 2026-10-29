package net.xlea99.lifeguard.mixin.supps_soap_lore_first;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.supplementaries.common.items.crafting.SoapClearRecipe;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.xlea99.lifeguard.entry.supps_soap_lore_first.SuppsSoapLoreFirst;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Soap takes an item's lore off before its color: while the item has lore, Supplementaries' color-washing recipe steps
 * aside so its lore-washing one is the one that matches.
 *
 * <p>Both match soap and a colored item with lore (a dye, wool), and the crafting grid takes whichever recipe it finds
 * first: always the color one, which carries the lore over (transmuteCopy), and white still counts as a color, so lore on
 * a dye could never come off. Now the first wash takes the lore and the next the color. Only while Supplementaries' lore
 * recipes are on (its {@code item_lore} flag), or there would be nothing left to wash the lore off.
 */
@Mixin(SoapClearRecipe.class)
public abstract class SoapClearRecipeMixin {
    @Shadow
    @Final
    private Ingredient soap;

    @ModifyReturnValue(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z",
        at = @At("RETURN"))
    private boolean lifeguard$loreFirst(boolean matched, CraftingInput input) {
        if (!matched || !SuppsSoapLoreFirst.ENTRY.active() || !CommonConfigs.Tweaks.ITEM_LORE.get())
            return matched;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && !this.soap.test(stack)
                && !stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines().isEmpty())
                return false;
        }
        return true;
    }
}
