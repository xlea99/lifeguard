package net.xlea99.lifeguard.mixin.supps_soap_washes_etcetera_labels;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.supplementaries.common.items.crafting.ItemLoreRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.xlea99.lifeguard.entry.supps_soap_washes_etcetera_labels.EtceteraLabels;
import net.xlea99.lifeguard.entry.supps_soap_washes_etcetera_labels.SuppsSoapWashesEtceteraLabels;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Supplementaries' soap recipe that clears an item's lore (its {@code item_lore} recipe type with {@code set_lore}
 * false) clears Etcetera's item labels too: soap and one labeled item, and out comes the item without its labels. The
 * same recipe, so the recipe viewer shows one soap recipe for both. When Supplementaries' own soap recipe is off (its
 * {@code item_lore} flag), Lifeguard's copy of it (data, same type) stands in.
 */
@Mixin(ItemLoreRecipe.class)
public abstract class ItemLoreRecipeMixin {
    @Shadow
    @Final
    private boolean setLore;
    @Shadow
    @Final
    private Ingredient requiredIngredient;

    @ModifyReturnValue(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z",
        at = @At("RETURN"))
    private boolean lifeguard$matchesLabeledItem(boolean matched, CraftingInput input) {
        if (matched || this.setLore || !SuppsSoapWashesEtceteraLabels.ENTRY.active())
            return matched;
        ItemStack soap = null;
        ItemStack item = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty())
                continue;
            if (soap == null && this.requiredIngredient.test(stack))
                soap = stack;
            else if (item == null)
                item = stack;
            else
                return false;
        }
        return soap != null && item != null && EtceteraLabels.has(item);
    }

    @ModifyReturnValue(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
        at = @At("RETURN"))
    private ItemStack lifeguard$washLabels(ItemStack result) {
        if (!this.setLore && SuppsSoapWashesEtceteraLabels.ENTRY.active())
            EtceteraLabels.strip(result);
        return result;
    }
}
