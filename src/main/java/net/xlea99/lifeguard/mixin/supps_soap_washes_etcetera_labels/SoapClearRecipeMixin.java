package net.xlea99.lifeguard.mixin.supps_soap_washes_etcetera_labels;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.supplementaries.common.items.crafting.SoapClearRecipe;
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
 * Soap takes an item's Etcetera labels off before its color: while the item has labels, Supplementaries' color-washing
 * recipe steps aside so the label-washing one (its lore recipe, {@link ItemLoreRecipeMixin}) is the one that matches.
 *
 * <p>Both match soap and a labeled colored item (a cyan dye), and the crafting grid takes whichever recipe it finds first:
 * always the color one, which turns the item white and carries its labels over (transmuteCopy), so a dye's labels could
 * never come off (white dye still counts as colored). Now the first wash takes the labels off and the next takes the
 * color, one layer at a time, and nothing comes off that wasn't asked for.
 */
@Mixin(SoapClearRecipe.class)
public abstract class SoapClearRecipeMixin {
    @Shadow
    @Final
    private Ingredient soap;

    @ModifyReturnValue(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z",
        at = @At("RETURN"))
    private boolean lifeguard$labelsFirst(boolean matched, CraftingInput input) {
        if (!matched || !SuppsSoapWashesEtceteraLabels.ENTRY.active())
            return matched;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && !this.soap.test(stack) && EtceteraLabels.has(stack))
                return false;
        }
        return true;
    }
}
